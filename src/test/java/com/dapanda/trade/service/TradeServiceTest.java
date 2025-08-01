package com.dapanda.trade.service;

import static com.dapanda.TestConstants.Member.*;
import static com.dapanda.TestConstants.MobileData.*;
import static com.dapanda.TestConstants.Pagination.DEFAULT_CURSOR_ID;
import static com.dapanda.TestConstants.Pagination.DEFAULT_SIZE_2;
import static com.dapanda.TestConstants.Plan.PROVIDING_DATA_AMOUNT_10;
import static com.dapanda.TestConstants.Product.*;
import static com.dapanda.TestConstants.Trade.TRADE_ID_1;
import static com.dapanda.TestConstants.Trade.TRADE_ID_2;
import static com.dapanda.TestConstants.Wifi.*;
import static com.dapanda.trade.entity.TradeType.PURCHASE_WIFI;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.dapanda.alarm.scheduler.WifiTradeNotificationScheduler;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.dto.response.CursorPageResponse.PageInfo;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.fcmToken.entity.FcmToken;
import com.dapanda.fcmToken.repository.FcmTokenRepository;
import com.dapanda.fcmToken.service.FcmTokenService;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.plan.entity.Plan;
import com.dapanda.plan.entity.PlanFixture;
import com.dapanda.plan.repository.PlanRepository;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.*;
import com.dapanda.trade.dto.*;
import com.dapanda.trade.dto.request.*;
import com.dapanda.trade.dto.response.*;
import com.dapanda.trade.entity.Trade;
import com.dapanda.trade.entity.TradeFixture;
import com.dapanda.trade.repository.TradeDetailsRepository;
import com.dapanda.trade.repository.TradeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
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
	private WifiRepository wifiRepository;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private TradeDetailsRepository tradeDetailsRepository;
	@Mock
	private PlanRepository planRepository;
	@Mock
	private FcmTokenService fcmTokenService;
	@Mock
	private FcmTokenRepository fcmTokenRepository;
	@Mock
	private WifiTradeNotificationScheduler wifiTradeNotificationScheduler;

	@InjectMocks
	private TradeService tradeService;


	@Nested
	@DisplayName("데이터 상품 일반 구매")
	class DefaultPurchaseMobileDataFull {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 통합 상품 일반 구매를 성공한다")
			void purchaseDataProductDefaultFullPurchase() {

				// given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				fcmTokenRepository.save(FcmToken.of("test_token", seller));
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_3000);
				Plan sellerPlan = PlanFixture.createPlan(seller, PROVIDING_DATA_AMOUNT_10);
				Plan buyerPlan = PlanFixture.createPlan(buyer, PROVIDING_DATA_AMOUNT_10);

				MobileData mobileData = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				Product product = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, MOBILE_DATA_ID, PRICE_3000, seller);

				DefaultPurchaseMobileDataRequest request = new DefaultPurchaseMobileDataRequest(
						PRODUCT_ID, MOBILE_DATA_ID, null);

				given(memberRepository.findByIdForUpdate(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findById(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findByIdForUpdate(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller));
				given(memberRepository.findById(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller));
				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(productRepository.findById(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(mobileDataRepository.findById(MOBILE_DATA_ID)).willReturn(
						Optional.of(mobileData));
				given(planRepository.findByMember(buyer)).willReturn(
						Optional.of(buyerPlan));
				given(planRepository.findByMember(seller)).willReturn(
						Optional.of(sellerPlan));

				// when
				tradeService.defaultPurchaseMobileData(BUYER_MEMBER_ID, request);

				// then
				Plan updateBuyerPlan = planRepository.findByMember(buyer).orElseThrow();
				Plan updateSellerPlan = planRepository.findByMember(seller).orElseThrow();

				assertThat(product.getState()).isEqualTo(ProductState.SOLD_OUT);
				assertThat(mobileData.getRemainAmount()).isEqualByComparingTo(
						BigDecimal.valueOf(0));
				assertThat(updateBuyerPlan.getCurrentDataAmount()).isEqualByComparingTo(
						PROVIDING_DATA_AMOUNT_10.add(DATA_AMOUNT_1));
				assertThat(updateSellerPlan.getCurrentDataAmount()).isEqualByComparingTo(
						PROVIDING_DATA_AMOUNT_10.subtract(DATA_AMOUNT_1));
				assertThat(buyer.getBuyingData()).isEqualByComparingTo(DATA_AMOUNT_1);
				assertThat(seller.getSellingData()).isEqualByComparingTo(DATA_AMOUNT_1);

			}

			@Test
			@DisplayName("데이터 분할 상품 일반 구매를 성공한다")
			void purchaseDataProductDefaultPartialPurchase() {

				// given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_3000);
				Plan sellerPlan = PlanFixture.createPlan(seller, PROVIDING_DATA_AMOUNT_10);
				Plan buyerPlan = PlanFixture.createPlan(buyer, PROVIDING_DATA_AMOUNT_10);

				MobileData mobileData = MobileDataFixture.createMobileDataSplitType(DATA_AMOUNT_2,
						REMAIN_AMOUNT_2, PRICE_PER_100MB_300);
				Product product = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, MOBILE_DATA_ID, PRICE_3000, seller);

				DefaultPurchaseMobileDataRequest request = new DefaultPurchaseMobileDataRequest(
						PRODUCT_ID, MOBILE_DATA_ID, DATA_AMOUNT_1);

				given(memberRepository.findByIdForUpdate(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findById(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findByIdForUpdate(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller));
				given(memberRepository.findById(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller));
				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(productRepository.findById(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(mobileDataRepository.findById(MOBILE_DATA_ID)).willReturn(
						Optional.of(mobileData));
				given(planRepository.findByMember(buyer)).willReturn(
						Optional.of(buyerPlan));
				given(planRepository.findByMember(seller)).willReturn(
						Optional.of(sellerPlan));

				// when
				tradeService.defaultPurchaseMobileData(BUYER_MEMBER_ID, request);

				// then
				assertThat(product.getState()).isEqualTo(ProductState.ACTIVE);
				assertThat(mobileData.getRemainAmount()).isEqualByComparingTo(
						DATA_AMOUNT_2.subtract(DATA_AMOUNT_1));
				assertThat(buyerPlan.getCurrentDataAmount()).isEqualByComparingTo(
						PROVIDING_DATA_AMOUNT_10.add(DATA_AMOUNT_1));
				assertThat(sellerPlan.getCurrentDataAmount()).isEqualByComparingTo(
						PROVIDING_DATA_AMOUNT_10.subtract(DATA_AMOUNT_1));
				assertThat(buyer.getBuyingData()).isEqualByComparingTo(DATA_AMOUNT_1);
				assertThat(seller.getSellingData()).isEqualByComparingTo(DATA_AMOUNT_1);
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

				DefaultPurchaseMobileDataRequest request = new DefaultPurchaseMobileDataRequest(
						PRODUCT_ID, MOBILE_DATA_ID, null);

				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));

				// when & then
				assertThatThrownBy(
						() -> tradeService.defaultPurchaseMobileData(BUYER_MEMBER_ID, request))
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

				DefaultPurchaseMobileDataRequest request = new DefaultPurchaseMobileDataRequest(
						PRODUCT_ID, MOBILE_DATA_ID, null);

				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));

				// when & then
				assertThatThrownBy(
						() -> tradeService.defaultPurchaseMobileData(SELLER_MEMBER_ID, request))
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
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				Product product = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, MOBILE_DATA_ID, PRICE_3000, seller);

				DefaultPurchaseMobileDataRequest request = new DefaultPurchaseMobileDataRequest(
						PRODUCT_ID, MOBILE_DATA_ID, null);

				given(memberRepository.findById(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findByIdForUpdate(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(productRepository.findById(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(mobileDataRepository.findById(MOBILE_DATA_ID)).willReturn(
						Optional.of(mobileData));

				// when & then
				assertThatThrownBy(
						() -> tradeService.defaultPurchaseMobileData(BUYER_MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INSUFFICIENT_CASH.getMessage());
			}

			@Test
			@DisplayName("데이터 분할 상품 일반 구매를 할 때 요청 데이터양이 상품의 잔여량보다 크면 예외를 던진다")
			void throwExceptionWhenBuyingDataPartialDefaultIfRequestIsGreaterThanRemain()
					throws Exception {

				// given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_3000);

				Plan buyerPlan = PlanFixture.createPlan(buyer, DATA_AMOUNT_2);
				Plan sellerPlan = PlanFixture.createPlan(seller, DATA_AMOUNT_2);

				MobileData mobileData = MobileDataFixture.createMobileDataSplitType(DATA_AMOUNT_2,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_150);
				Product product = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, MOBILE_DATA_ID, PRICE_3000, seller);

				DefaultPurchaseMobileDataRequest request = new DefaultPurchaseMobileDataRequest(
						PRODUCT_ID, MOBILE_DATA_ID, DATA_AMOUNT_2);

				given(memberRepository.findById(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findByIdForUpdate(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findByIdForUpdate(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller));
				given(productRepository.findById(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(mobileDataRepository.findById(MOBILE_DATA_ID)).willReturn(
						Optional.of(mobileData));
				given(planRepository.findByMember(buyer)).willReturn(Optional.of(buyerPlan));
				given(planRepository.findByMember(seller)).willReturn(Optional.of(sellerPlan));

				// when & then
				assertThatThrownBy(
						() -> tradeService.defaultPurchaseMobileData(BUYER_MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INVALID_REMAIN_DATA_AMOUNT.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("데이터 상품 자투리 조회")
	class FindMobileDataScrap {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 상품 자투리 조합이 존재할 때 조회를 성공한다")
			void findDataProductScrapWhenExist() {

				// given
				Member seller1 = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member seller2 = MemberFixture.createMember1WithId(SELLER_MEMBER_ID + 1);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_3000);

				MobileData mobileData1 = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_150);
				MobileData mobileData2 = MobileDataFixture.createMobileDataSplitType(DATA_AMOUNT_2,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				Product product1 = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, MOBILE_DATA_ID, PRICE_1500, seller1);
				Product product2 = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID + 1, MOBILE_DATA_ID + 1, PRICE_3000, seller2);

				MobileDataScrap mobileDataScrap1 = TradeFixture.createMobileDataScrap(product1,
						mobileData1, PRICE_1500, DATA_AMOUNT_1);
				MobileDataScrap mobileDataScrap2 = TradeFixture.createMobileDataScrap(product2,
						mobileData2, PRICE_3000, DATA_AMOUNT_1);

				BigDecimal dataAmount = DATA_AMOUNT_2;

				given(productRepository.findMobileDataScrap(dataAmount,
						BUYER_MEMBER_ID)).willReturn(new ArrayList<>(
						List.of(mobileDataScrap1, mobileDataScrap2)));

				// when
				FindMobileDataScrapResponse response = tradeService.findMobileDataScrap(
						dataAmount, BUYER_MEMBER_ID);

				// then
				assertThat(response.getTotalAmount()).isEqualTo(DATA_AMOUNT_2);
				assertThat(response.getTotalPrice()).isEqualTo(
						PRICE_1500 + PRICE_3000); // 조합된 상품의 총 가격
				assertThat(response.getCombinations().size()).isEqualTo(2); // 조합된 상품 개수 확인

				MobileDataScrap result1 = response.getCombinations().get(0);
				MobileDataScrap result2 = response.getCombinations().get(1);

				assertThat(result1.getProductId()).isEqualTo(product1.getId());
				assertThat(result1.getMobileDataId()).isEqualTo(mobileData1.getId());
				assertThat(result1.getPrice()).isEqualTo(product1.getPrice());
				assertThat(result1.getRemainAmount()).isEqualTo(mobileData1.getRemainAmount());
				assertThat(result1.getPricePer100MB()).isEqualTo(mobileData1.getPricePer100MB());
				assertThat(result1.isSplitType()).isEqualTo(mobileData1.isSplitType());

				assertThat(result2.getProductId()).isEqualTo(product2.getId());
				assertThat(result2.getMobileDataId()).isEqualTo(mobileData2.getId());
				assertThat(result2.getPrice()).isEqualTo(product2.getPrice());
				assertThat(result2.getRemainAmount()).isEqualTo(mobileData2.getRemainAmount());
				assertThat(result2.getPricePer100MB()).isEqualTo(mobileData2.getPricePer100MB());
				assertThat(result2.isSplitType()).isEqualTo(mobileData2.isSplitType());
			}

			@Test
			@DisplayName("데이터 상품 자투리 조합이 존재하지 않을 때 조회를 성공한다")
			void findDataProductScrapWhenNotExist() {

				// given
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);

				BigDecimal dataAmount = DATA_AMOUNT_2;

				given(productRepository.findMobileDataScrap(dataAmount,
						BUYER_MEMBER_ID)).willReturn(
						new ArrayList<>());

				// when
				FindMobileDataScrapResponse response = tradeService.findMobileDataScrap(
						dataAmount, BUYER_MEMBER_ID);

				// then
				assertThat(response.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
				assertThat(response.getTotalPrice()).isEqualTo(0); // 조합된 상품의 총 가격
				assertThat(response.getCombinations().size()).isEqualTo(0); // 조합된 상품 개수 확인
			}
		}
	}

	@Nested
	@DisplayName("데이터 상품 자투리 구매")
	class ScrapPurchaseMobileData {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 상품 자투리 구매를 성공한다")
			void scrapPurchaseMobileData() {

				// given
				Member seller1 = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member seller2 = MemberFixture.createMember1WithId(SELLER_MEMBER_ID + 1);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);

				Plan buyerPlan = PlanFixture.createPlan(buyer, DATA_AMOUNT_2);
				Plan sellerPlan1 = PlanFixture.createPlan(seller1, DATA_AMOUNT_2);
				Plan sellerPlan2 = PlanFixture.createPlan(seller2, DATA_AMOUNT_2);

				MobileData mobileData1 = MobileDataFixture.createMobileDataWithId(MOBILE_DATA_ID,
						DATA_AMOUNT_1, REMAIN_AMOUNT_1, PRICE_PER_100MB_150);
				MobileData mobileData2 = MobileDataFixture.createMobileDataSplitTypeWithId(
						MOBILE_DATA_ID + 1, DATA_AMOUNT_2,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				Product product1 = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, mobileData1.getId(), PRICE_1500, seller1);
				Product product2 = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID + 1, mobileData2.getId(), PRICE_3000, seller2);

				MobileDataScrap mobileDataScrap1 = TradeFixture.createMobileDataScrap(product1,
						mobileData1, PRICE_1500, DATA_AMOUNT_1);
				MobileDataScrap mobileDataScrap2 = TradeFixture.createMobileDataScrap(product2,
						mobileData2, PRICE_3000, DATA_AMOUNT_1);
				List<MobileDataScrap> mobileDataScrapList = new ArrayList<>(
						Arrays.asList(mobileDataScrap1, mobileDataScrap2));

				ScrapPurchaseMobileDataRequest request = new ScrapPurchaseMobileDataRequest(
						DATA_AMOUNT_2,
						PRICE_1500 + PRICE_3000, mobileDataScrapList);

				given(memberRepository.findById(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findByIdForUpdate(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findById(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller1));
				given(memberRepository.findByIdForUpdate(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller1));
				given(memberRepository.findById(SELLER_MEMBER_ID + 1)).willReturn(
						Optional.of(seller2));
				given(memberRepository.findByIdForUpdate(SELLER_MEMBER_ID + 1)).willReturn(
						Optional.of(seller2));
				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product1));
				given(productRepository.findByIdForUpdate(PRODUCT_ID + 1)).willReturn(
						Optional.of(product2));
				given(mobileDataRepository.findByIdForUpdate(MOBILE_DATA_ID)).willReturn(
						Optional.of(mobileData1));
				given(mobileDataRepository.findByIdForUpdate(MOBILE_DATA_ID + 1)).willReturn(
						Optional.of(mobileData2));
				given(planRepository.findByMember(buyer)).willReturn(Optional.of(buyerPlan));
				given(planRepository.findByMember(seller1)).willReturn(Optional.of(sellerPlan1));
				given(planRepository.findByMember(seller2)).willReturn(Optional.of(sellerPlan2));

				// when
				tradeService.scrapPurchaseMobileData(BUYER_MEMBER_ID, request);

				// then
				assertThat(mobileData1.getRemainAmount()).isEqualTo(BigDecimal.valueOf(0.0));
				assertThat(mobileData2.getRemainAmount()).isEqualTo(BigDecimal.valueOf(0.0));

				assertThat(buyer.getCash()).isEqualTo(CASH_5000 - request.totalPrice());
				assertThat(buyer.getBuyingData()).isEqualTo(request.totalAmount());

				assertThat(seller1.getSellingData()).isEqualTo(DATA_AMOUNT_1);
				assertThat(seller1.getCash()).isEqualTo(PRICE_1500);

				assertThat(seller2.getSellingData()).isEqualTo(DATA_AMOUNT_1);
				assertThat(seller2.getCash()).isEqualTo(PRICE_3000);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("데이터 상품 자투리 구매를 할 때 보유 캐시가 충분하지 않으면 예외를 던진다")
			void throwExceptionWhenCashInsufficient() {

				// given
				Member seller1 = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member seller2 = MemberFixture.createMember1WithId(SELLER_MEMBER_ID + 1);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", 0);

				MobileData mobileData1 = MobileDataFixture.createMobileDataWithId(MOBILE_DATA_ID,
						DATA_AMOUNT_1, REMAIN_AMOUNT_1, PRICE_PER_100MB_150);
				MobileData mobileData2 = MobileDataFixture.createMobileDataSplitTypeWithId(
						MOBILE_DATA_ID + 1, DATA_AMOUNT_2,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				Product product1 = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, mobileData1.getId(), PRICE_1500, seller1);
				Product product2 = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID + 1, mobileData2.getId(), PRICE_3000, seller2);

				MobileDataScrap mobileDataScrap1 = TradeFixture.createMobileDataScrap(product1,
						mobileData1, PRICE_1500, DATA_AMOUNT_1);
				MobileDataScrap mobileDataScrap2 = TradeFixture.createMobileDataScrap(product2,
						mobileData2, PRICE_3000, DATA_AMOUNT_1);
				List<MobileDataScrap> mobileDataScrapList = new ArrayList<>(
						Arrays.asList(mobileDataScrap1, mobileDataScrap2));

				ScrapPurchaseMobileDataRequest request = new ScrapPurchaseMobileDataRequest(
						DATA_AMOUNT_2,
						PRICE_1500 + PRICE_3000, mobileDataScrapList);

				given(memberRepository.findByIdForUpdate(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findById(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));

				// when & then
				assertThatThrownBy(
						() -> tradeService.scrapPurchaseMobileData(BUYER_MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INSUFFICIENT_CASH.getMessage());
			}

			@Test
			@DisplayName("데이터 상품 자투리 구매를 할 때 잔여 데이터양이 유효하지 않으면 예외를 던진다")
			void throwExceptionWhenRemainAmountInsufficient() {

				// given
				Member seller1 = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member seller2 = MemberFixture.createMember1WithId(SELLER_MEMBER_ID + 1);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);

				Plan buyerPlan = PlanFixture.createPlan(buyer, DATA_AMOUNT_2);

				MobileData mobileData1 = MobileDataFixture.createMobileDataWithId(MOBILE_DATA_ID,
						DATA_AMOUNT_1, BigDecimal.ZERO, PRICE_PER_100MB_150);
				MobileData mobileData2 = MobileDataFixture.createMobileDataSplitTypeWithId(
						MOBILE_DATA_ID + 1, DATA_AMOUNT_2, REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				Product product1 = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID, mobileData1.getId(), PRICE_1500, seller1);
				Product product2 = ProductFixture.createMobileDataProductWithId(
						PRODUCT_ID + 1, mobileData2.getId(), PRICE_3000, seller2);

				MobileDataScrap mobileDataScrap1 = TradeFixture.createMobileDataScrap(product1,
						mobileData1, PRICE_1500, DATA_AMOUNT_1);
				MobileDataScrap mobileDataScrap2 = TradeFixture.createMobileDataScrap(product2,
						mobileData2, PRICE_3000, DATA_AMOUNT_1);
				List<MobileDataScrap> mobileDataScrapList = new ArrayList<>(
						Arrays.asList(mobileDataScrap1, mobileDataScrap2));

				ScrapPurchaseMobileDataRequest request = new ScrapPurchaseMobileDataRequest(
						DATA_AMOUNT_2,
						PRICE_1500 + PRICE_3000, mobileDataScrapList);

				given(memberRepository.findById(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findByIdForUpdate(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findById(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller1));
				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product1));
				given(mobileDataRepository.findByIdForUpdate(MOBILE_DATA_ID)).willReturn(
						Optional.of(mobileData1));
				given(planRepository.findByMember(buyer)).willReturn(Optional.of(buyerPlan));

				// when & then
				assertThatThrownBy(
						() -> tradeService.scrapPurchaseMobileData(BUYER_MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INVALID_REMAIN_DATA_AMOUNT.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("와이파이 상품 구매")
	class PurchaseWifi {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("와이파이 상품 구매를 성공한다")
			void purchaseWifi() {

				// given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);

				Wifi wifi = WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
						START_DATETIME, END_DATETIME);
				ReflectionTestUtils.setField(wifi, "id", WIFI_ID);

				Product product = ProductFixture.createWifiProductWithId(
						PRODUCT_ID, WIFI_ID, SELLER_MEMBER_ID, PRICE_500);

				PurchaseWifiRequest request = new PurchaseWifiRequest(PRODUCT_ID, WIFI_ID,
						LocalDateTime.of(2025, 3, 4, 10, 0), LocalDateTime.of(2025, 3, 4, 10, 30));

				given(memberRepository.findByIdForUpdate(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findById(BUYER_MEMBER_ID)).willReturn(
						Optional.of(buyer));
				given(memberRepository.findByIdForUpdate(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller));
				given(memberRepository.findById(SELLER_MEMBER_ID)).willReturn(
						Optional.of(seller));
				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(wifiRepository.findById(WIFI_ID)).willReturn(
						Optional.of(wifi));

				// when
				tradeService.purchaseWifi(BUYER_MEMBER_ID, request);

				// then
				assertThat(buyer.getCash()).isEqualTo(CASH_5000 - PRICE_500 * 3);
				assertThat(seller.getCash()).isEqualTo(PRICE_500 * 3);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("와이파이 상품 구매를 할 때 입력받은 시간이 영업 시간을 넘으면 예외를 던진다")
			void throwExceptionWhenInvalidOperationTime() {

				// given
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);

				Wifi wifi = WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
						START_DATETIME, END_DATETIME);
				ReflectionTestUtils.setField(wifi, "id", WIFI_ID);

				Product product = ProductFixture.createWifiProductWithId(
						PRODUCT_ID, WIFI_ID, SELLER_MEMBER_ID, PRICE_500);

				PurchaseWifiRequest request = new PurchaseWifiRequest(PRODUCT_ID, WIFI_ID,
						LocalDateTime.of(2025, 3, 4, 23, 0), LocalDateTime.of(2025, 3, 4, 23, 30));

				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));
				given(wifiRepository.findById(WIFI_ID)).willReturn(
						Optional.of(wifi));

				// when & then
				assertThatThrownBy(() -> tradeService.purchaseWifi(BUYER_MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INVALID_WIFI_OPERATION_TIME.getMessage());
			}

			@Test
			@DisplayName("와이파이 상품이 존재하지 않으면 예외를 던진다")
			void throwExceptionWhenNotFoundWifi() {

				// given
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);

				Wifi wifi = WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
						START_DATETIME, END_DATETIME);
				ReflectionTestUtils.setField(wifi, "id", WIFI_ID);

				Product product = ProductFixture.createWifiProductWithId(
						PRODUCT_ID, WIFI_ID, SELLER_MEMBER_ID, PRICE_500);

				PurchaseWifiRequest request = new PurchaseWifiRequest(PRODUCT_ID, WIFI_ID + 1,
						LocalDateTime.of(2025, 3, 4, 10, 0), LocalDateTime.of(2025, 3, 4, 10, 30));

				given(productRepository.findByIdForUpdate(PRODUCT_ID)).willReturn(
						Optional.of(product));

				// when & then
				assertThatThrownBy(() -> tradeService.purchaseWifi(BUYER_MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.WIFI_NOT_FOUND.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("상품 거래(구매) 내역 조회")
	class FindPurchaseHistory {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("상품 거래(구매) 내역 조회를 성공한다")
			void findPurchaseHistory() {

				// given
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);
				ReflectionTestUtils.setField(buyer, "cash", CASH_3000);

				Trade trade1 = TradeFixture.createTradeWifi(buyer);
				Trade trade2 = TradeFixture.createTradeWifi(buyer);

				PurchaseHistorySummary summary1 = new PurchaseHistorySummary(

						TRADE_ID_1,
						PURCHASE_WIFI,
						BigDecimal.valueOf(0),
						TITLE,
						PROFILE_IMAGE_URL,
						trade1.getCreatedAt()
				);

				PurchaseHistorySummary summary2 = new PurchaseHistorySummary(

						TRADE_ID_2,
						PURCHASE_WIFI,
						BigDecimal.valueOf(0),
						TITLE + 1,
						PROFILE_IMAGE_URL,
						trade2.getCreatedAt()
				);

				given(tradeRepository.countTradeHistoryByMemberId(BUYER_MEMBER_ID)).willReturn(2L);
				given(tradeRepository.findTradeHistoryByCursor(null, DEFAULT_SIZE_2,
						BUYER_MEMBER_ID)).willReturn(CursorPageResponse.of(
						List.of(summary1, summary2), PageInfo.of(null, false, 2)
				));

				// when
				FindTradeHistoryResponse response = tradeService.findTradeHistory(null,
						DEFAULT_SIZE_2, BUYER_MEMBER_ID);

				// then
				PurchaseHistorySummary result1 = response.getTrades().getData().get(0);
				PurchaseHistorySummary result2 = response.getTrades().getData().get(1);

				assertThat(response.getTradeCount()).isEqualTo(DEFAULT_SIZE_2);
				assertThat(response.getTrades().getData().size()).isEqualTo(DEFAULT_SIZE_2);

				assertThat(result1.getTradeType()).isEqualTo(PURCHASE_WIFI);
				assertThat(result1.getTitle()).isEqualTo(TITLE);

				assertThat(result2.getTradeType()).isEqualTo(PURCHASE_WIFI);
				assertThat(result2.getTitle()).isEqualTo(TITLE + 1);
			}
		}
	}

	@Nested
	@DisplayName("캐시 내역 조회")
	class FindCashHistory {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("캐시 내역 조회를 성공한다")
			void findCashHistory() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);

				Trade wifiTrade = TradeFixture.createTradeWifi(member);
				Trade mobileDataTrade = TradeFixture.createTradeMobileDataDefault(member);
				Trade chargeTrade = TradeFixture.createTradeCharge(member);
				Trade saleTrade = TradeFixture.createTradeSale(member);

				int year = LocalDate.now().getYear();
				int month = LocalDate.now().getMonthValue();

				CursorPageResponse<CashHistorySummary> mockPageResponse =
						CursorPageResponse.of(
								List.of(
										new CashHistorySummary(TRADE_ID_1, wifiTrade.getTradeType(),
												wifiTrade.getTradingPrice(), "0.5", "구매",
												LocalDateTime.now()),
										new CashHistorySummary(TRADE_ID_2,
												chargeTrade.getTradeType(),
												wifiTrade.getTradingPrice(), "60", "구매",
												LocalDateTime.now())
								),
								PageInfo.of(null, false, 2)
						);

				CashHistoryMonthlySummary mockMonthlySummary =
						new CashHistoryMonthlySummary(3500, 4000, 1000, 500, 1000);

				given(tradeRepository.findCashHistoryByCursor(DEFAULT_CURSOR_ID, DEFAULT_SIZE_2,
						MEMBER_ID, year, month)).willReturn(mockPageResponse);
				given(tradeRepository.calculateMonthlySummary(MEMBER_ID, year, month))
						.willReturn(mockMonthlySummary);

				// when
				FindCashHistoryResponse response = tradeService.findCashHistory(null,
						DEFAULT_SIZE_2, MEMBER_ID, year, month);

				// then
				assertThat(response.getCashHistoryMonthlySummary()).isEqualTo(mockMonthlySummary);
				assertThat(response.getCashHistorySummary()).isEqualTo(mockPageResponse);
			}
		}
	}
}
