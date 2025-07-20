package com.dapanda.trade.service;

import static com.dapanda.TestConstants.Member.BUYER_MEMBER_ID;
import static com.dapanda.TestConstants.Member.CASH_5000;
import static com.dapanda.TestConstants.Member.SELLER_MEMBER_ID;
import static com.dapanda.TestConstants.MobileData.DATA_AMOUNT_1;
import static com.dapanda.TestConstants.MobileData.MOBILE_DATA_ID;
import static com.dapanda.TestConstants.MobileData.PRICE_PER_100MB;
import static com.dapanda.TestConstants.MobileData.REMAIN_AMOUNT_1;
import static com.dapanda.TestConstants.Plan.PROVIDING_DATA_AMOUNT_10;
import static com.dapanda.TestConstants.Product.PRICE_3000;
import static com.dapanda.TestConstants.Product.PRODUCT_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.plan.entity.Plan;
import com.dapanda.plan.repository.PlanRepository;
import com.dapanda.plan.service.entity.PlanFixture;
import com.dapanda.product.entity.MobileData;
import com.dapanda.product.entity.MobileDataFixture;
import com.dapanda.product.entity.Product;
import com.dapanda.product.entity.ProductFixture;
import com.dapanda.product.entity.ProductState;
import com.dapanda.product.repository.MobileDataRepository;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.trade.dto.request.TradeMobileDataDefaultRequest;
import com.dapanda.trade.repository.TradeDetailsRepository;
import com.dapanda.trade.repository.TradeRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("거래 서비스 테스트")
class TradeServiceTest {

	@Mock
	private TradeRepository tradeRepository;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private MobileDataRepository mobileDataRepository;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private TradeDetailsRepository tradeDetailsRepository;
	@Mock
	private PlanRepository planRepository;

	@InjectMocks
	private TradeService tradeService;

	@Nested
	@DisplayName("데이터 통합 상품 일반 구매")
	class FindMobileData {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 통합 상품 일반 구매를 성공한다")
			void purchaseDataProductDefaultFullPurchase() {

				// given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", 3000);
				Plan sellerPlan = PlanFixture.createPlan(seller, PROVIDING_DATA_AMOUNT_10);
				Plan buyerPlan = PlanFixture.createPlan(buyer, PROVIDING_DATA_AMOUNT_10);

				MobileData mobileData = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB);
				Product product = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, MOBILE_DATA_ID, PRICE_3000, seller);

				TradeMobileDataDefaultRequest request = new TradeMobileDataDefaultRequest(
						PRODUCT_ID, MOBILE_DATA_ID);

				given(memberRepository.findByIdForUpdate(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findByIdForUpdate(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller));
				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(mobileDataRepository.findById(MOBILE_DATA_ID)).willReturn(
						Optional.of(mobileData));
				given(planRepository.findByMember(buyer)).willReturn(
						Optional.of(buyerPlan));
				given(planRepository.findByMember(seller)).willReturn(
						Optional.of(sellerPlan));

				// when
				tradeService.mobileDataDefault(BUYER_MEMBER_ID, request);

				// then
				assertThat(product.getState()).isEqualTo(ProductState.SOLD_OUT);
				assertThat(mobileData.getRemainAmount()).isEqualTo(0);
				assertThat(buyerPlan.getProvidingDataAmount()).isEqualTo(
						PROVIDING_DATA_AMOUNT_10 + DATA_AMOUNT_1);
				assertThat(sellerPlan.getProvidingDataAmount()).isEqualTo(
						PROVIDING_DATA_AMOUNT_10 - DATA_AMOUNT_1);
				assertThat(buyer.getBuyingData()).isEqualTo(DATA_AMOUNT_1);
				assertThat(seller.getSellingData()).isEqualTo(DATA_AMOUNT_1);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("데이터 통합 상품 일반 구매를 할 때 이미 판매 완료된 상품이면 예외를 던진다")
			void throwExceptionWhenBuyingDataFullDefault() throws Exception {

				// given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);

				Product product = ProductFixture.createMobileDataProductSoldOutWithId(
						PRODUCT_ID, MOBILE_DATA_ID, PRICE_3000, seller);

				TradeMobileDataDefaultRequest request = new TradeMobileDataDefaultRequest(
						PRODUCT_ID, MOBILE_DATA_ID);

				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));

				// when & then
				assertThatThrownBy(() -> tradeService.mobileDataDefault(BUYER_MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.ALREADY_SOLD_OUT.getMessage());
			}

			@Test
			@DisplayName("데이터 통합 상품 일반 구매를 할 때 자신이 등록한 상품이면 예외를 던진다")
			void throwExceptionWhenBuyingSelfProduct() throws Exception {

				// given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				ReflectionTestUtils.setField(seller, "cash", CASH_5000);

				Product product = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, MOBILE_DATA_ID, PRICE_3000, seller);

				TradeMobileDataDefaultRequest request = new TradeMobileDataDefaultRequest(
						PRODUCT_ID, MOBILE_DATA_ID);

				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));

				// when & then
				assertThatThrownBy(() -> tradeService.mobileDataDefault(SELLER_MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.CANNOT_PURCHASE_OWN_PRODUCT.getMessage());
			}

			@Test
			@DisplayName("데이터 통합 상품 일반 구매를 할 때 보유 캐시가 부족하면 예외를 던진다")
			void throwExceptionWhenCashInSufficient() throws Exception {

				// given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", 0);
				Plan sellerPlan = PlanFixture.createPlan(seller, PROVIDING_DATA_AMOUNT_10);
				Plan buyerPlan = PlanFixture.createPlan(buyer, PROVIDING_DATA_AMOUNT_10);

				MobileData mobileData = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB);
				Product product = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, MOBILE_DATA_ID, PRICE_3000, seller);

				TradeMobileDataDefaultRequest request = new TradeMobileDataDefaultRequest(
						PRODUCT_ID, MOBILE_DATA_ID);

				given(memberRepository.findByIdForUpdate(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findByIdForUpdate(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller));
				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(mobileDataRepository.findById(MOBILE_DATA_ID)).willReturn(
						Optional.of(mobileData));

				// when & then
				assertThatThrownBy(() -> tradeService.mobileDataDefault(BUYER_MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INSUFFICIENT_CASH.getMessage());
			}
		}
	}
}
