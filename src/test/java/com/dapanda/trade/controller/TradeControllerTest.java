package com.dapanda.trade.controller;


import static com.dapanda.TestConstants.Member.CASH_5000;
import static com.dapanda.TestConstants.MobileData.DATA_AMOUNT_1;
import static com.dapanda.TestConstants.MobileData.DATA_AMOUNT_2;
import static com.dapanda.TestConstants.MobileData.PRICE_PER_100MB;
import static com.dapanda.TestConstants.MobileData.REMAIN_AMOUNT_1;
import static com.dapanda.TestConstants.MobileData.REMAIN_AMOUNT_2;
import static com.dapanda.TestConstants.Plan.PROVIDING_DATA_AMOUNT_10;
import static com.dapanda.TestConstants.Product.PRICE_3000;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
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
import com.dapanda.product.repository.WifiRepository;
import com.dapanda.trade.dto.request.TradeMobileDataDefaultRequest;
import com.dapanda.trade.repository.TradeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("거래 컨트롤러 테스트")
class TradeControllerTest {

	@Autowired
	private WebApplicationContext context;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private MobileDataRepository mobileDataRepository;
	@Autowired
	private WifiRepository wifiRepository;

	@Autowired
	private TradeRepository tradeRepository;

	private MockMvc mockMvc;

	@Autowired
	private PlanRepository planRepository;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);

		cleanupDatabase();
	}

	private void cleanupDatabase() {

		entityManager.clear();

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

		jdbcTemplate.execute("TRUNCATE TABLE wifi");
		jdbcTemplate.execute("TRUNCATE TABLE mobile_data");
		jdbcTemplate.execute("TRUNCATE TABLE product");
		jdbcTemplate.execute("TRUNCATE TABLE member");
		jdbcTemplate.execute("TRUNCATE TABLE plan");
		jdbcTemplate.execute("TRUNCATE TABLE trade");
		jdbcTemplate.execute("TRUNCATE TABLE trade_details");

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
	}

	@Nested
	@DisplayName("데이터 상품 일반 구매 API")
	class MobileDataFullDefaultPurchase {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 통합 상품 일반 구매를 성공한다")
			void purchaseDataProductDefaultFullPurchase() throws Exception {

				// given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);
				memberRepository.save(buyer);

				Plan sellerPlan = planRepository.save(
						PlanFixture.createPlan(seller, PROVIDING_DATA_AMOUNT_10));
				Plan buyerPlan = planRepository.save(
						PlanFixture.createPlan(buyer, PROVIDING_DATA_AMOUNT_10));

				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(DATA_AMOUNT_1, REMAIN_AMOUNT_1,
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(),
								seller));

				TradeMobileDataDefaultRequest request = new TradeMobileDataDefaultRequest(
						product.getId(), mobileData.getId(), null);

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(buyer.getId());

				// when & then
				mockMvc.perform(post("/api/trades/mobile-data/default")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.tradeId").exists())
						.andDo(document("trade/post-mobile-data-full-default",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("mobileDataId").description("데이터 아이디 (필수)"),
										fieldWithPath("dataAmount").description(
												"구매할 데이터양 (필수 X, 분할 구매는 필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.tradeId").description("생성된 거래 아이디")
								))
						);

				Product soldOutProduct = productRepository.findById(product.getId()).orElseThrow();
				MobileData soldOutMobileData = mobileDataRepository.findById(mobileData.getId())
						.orElseThrow();
				Plan afterBuyerPlan = planRepository.findById(buyerPlan.getId()).orElseThrow();
				Plan afterSellerPlan = planRepository.findById(sellerPlan.getId()).orElseThrow();
				Member afterBuyer = memberRepository.findById(buyer.getId()).orElseThrow();
				Member afterSeller = memberRepository.findById(seller.getId()).orElseThrow();

				assertThat(soldOutProduct.getState()).isEqualTo(ProductState.SOLD_OUT);
				assertThat(soldOutMobileData.getRemainAmount()).isEqualTo(0);
				assertThat(afterBuyerPlan.getProvidingDataAmount()).isEqualTo(
						PROVIDING_DATA_AMOUNT_10 + mobileData.getDataAmount());
				assertThat(afterSellerPlan.getProvidingDataAmount()).isEqualTo(
						PROVIDING_DATA_AMOUNT_10 - mobileData.getDataAmount());
				assertThat(afterBuyer.getBuyingData()).isEqualTo(mobileData.getDataAmount());
				assertThat(afterSeller.getSellingData()).isEqualTo(mobileData.getDataAmount());
			}

			@Test
			@DisplayName("데이터 분할 상품 일반 구매를 성공한다")
			void purchaseDataProductDefaultPartialPurchase() throws Exception {

				// given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);
				memberRepository.save(buyer);

				Plan sellerPlan = planRepository.save(
						PlanFixture.createPlan(seller, PROVIDING_DATA_AMOUNT_10));
				Plan buyerPlan = planRepository.save(
						PlanFixture.createPlan(buyer, PROVIDING_DATA_AMOUNT_10));

				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileDataSplitType(DATA_AMOUNT_2, REMAIN_AMOUNT_2,
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(),
								seller));

				TradeMobileDataDefaultRequest request = new TradeMobileDataDefaultRequest(
						product.getId(), mobileData.getId(), DATA_AMOUNT_1);

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(buyer.getId());

				// when & then
				mockMvc.perform(post("/api/trades/mobile-data/default")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.tradeId").exists())
						.andDo(document("trade/post-mobile-data-partial-default",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("mobileDataId").description("데이터 아이디 (필수)"),
										fieldWithPath("dataAmount").description(
												"구매할 데이터양 (필수 X, 분할 구매는 필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.tradeId").description("생성된 거래 아이디")
								))
						);

				Product soldOutProduct = productRepository.findById(product.getId()).orElseThrow();
				MobileData soldOutMobileData = mobileDataRepository.findById(mobileData.getId())
						.orElseThrow();
				Plan afterBuyerPlan = planRepository.findById(buyerPlan.getId()).orElseThrow();
				Plan afterSellerPlan = planRepository.findById(sellerPlan.getId()).orElseThrow();
				Member afterBuyer = memberRepository.findById(buyer.getId()).orElseThrow();
				Member afterSeller = memberRepository.findById(seller.getId()).orElseThrow();

				assertThat(soldOutProduct.getState()).isEqualTo(ProductState.ACTIVE);
				assertThat(soldOutMobileData.getRemainAmount()).isEqualTo(
						DATA_AMOUNT_2 - DATA_AMOUNT_1);
				assertThat(afterBuyerPlan.getProvidingDataAmount()).isEqualTo(
						PROVIDING_DATA_AMOUNT_10 + DATA_AMOUNT_1);
				assertThat(afterSellerPlan.getProvidingDataAmount()).isEqualTo(
						PROVIDING_DATA_AMOUNT_10 - DATA_AMOUNT_1);
				assertThat(afterBuyer.getBuyingData()).isEqualTo(DATA_AMOUNT_1);
				assertThat(afterSeller.getSellingData()).isEqualTo(DATA_AMOUNT_1);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("데이터 상품 일반 구매를 할 때 이미 판매 완료된 상품이면 예외를 던진다")
			void throwExceptionWhenBuyingDataFullDefault() throws Exception {

				// given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);
				memberRepository.save(buyer);

				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(DATA_AMOUNT_1, REMAIN_AMOUNT_1,
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProductInactive(PRICE_3000,
								mobileData.getId(), seller));

				TradeMobileDataDefaultRequest request = new TradeMobileDataDefaultRequest(
						product.getId(), mobileData.getId(), null);

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(buyer.getId());

				// when & then
				mockMvc.perform(post("/api/trades/mobile-data/default")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(ResultCode.ALREADY_SOLD_OUT.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.ALREADY_SOLD_OUT.getMessage()))
						.andDo(document("trade/post-mobile-data-default-already-sold-out-error",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("mobileDataId").description("데이터 아이디 (필수)"),
										fieldWithPath("dataAmount").description(
												"구매할 데이터양 (필수 X, 분할 구매는 필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}

			@Test
			@DisplayName("데이터 상품 일반 구매를 할 때 보유 캐시가 부족하면 예외를 던진다")
			void throwExceptionWhenCashInSufficient() throws Exception {

				// given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());
				ReflectionTestUtils.setField(buyer, "cash", 0);
				memberRepository.save(buyer);

				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(DATA_AMOUNT_1, REMAIN_AMOUNT_1,
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000,
								mobileData.getId(), seller));

				TradeMobileDataDefaultRequest request = new TradeMobileDataDefaultRequest(
						product.getId(), mobileData.getId(), null);

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(buyer.getId());

				// when & then
				mockMvc.perform(post("/api/trades/mobile-data/default")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(ResultCode.INSUFFICIENT_CASH.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.INSUFFICIENT_CASH.getMessage()))
						.andDo(document("trade/post-mobile-data-default-insufficient-cash-error",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("mobileDataId").description("데이터 아이디 (필수)"),
										fieldWithPath("dataAmount").description(
												"구매할 데이터양 (필수 X, 분할 구매는 필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}

			@Test
			@DisplayName("데이터 상품 일반 구매를 할 때 자신이 등록한 상품이면 예외를 던진다")
			void throwExceptionWhenBuyingSelfProduct() throws Exception {

				// given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);
				memberRepository.save(buyer);

				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(DATA_AMOUNT_1, REMAIN_AMOUNT_1,
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000,
								mobileData.getId(), seller));

				TradeMobileDataDefaultRequest request = new TradeMobileDataDefaultRequest(
						product.getId(), mobileData.getId(), null);

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(seller.getId());

				// when & then
				mockMvc.perform(post("/api/trades/mobile-data/default")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(
								ResultCode.CANNOT_PURCHASE_OWN_PRODUCT.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.CANNOT_PURCHASE_OWN_PRODUCT.getMessage()))
						.andDo(document("trade/post-mobile-data-default-own-product-error",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("mobileDataId").description("데이터 아이디 (필수)"),
										fieldWithPath("dataAmount").description(
												"구매할 데이터양 (필수 X, 분할 구매는 필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}

			@Test
			@DisplayName("데이터 분할 상품 일반 구매를 할 때 요청 데이터양이 상품의 잔여량보다 크면 예외를 던진다")
			void throwExceptionWhenBuyingDataPartialDefaultIfRequestIsGreaterThanRemain()
					throws Exception {

				// given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());
				ReflectionTestUtils.setField(buyer, "cash", CASH_5000);
				memberRepository.save(buyer);

				Plan sellerPlan = planRepository.save(
						PlanFixture.createPlan(seller, PROVIDING_DATA_AMOUNT_10));
				Plan buyerPlan = planRepository.save(
						PlanFixture.createPlan(buyer, PROVIDING_DATA_AMOUNT_10));

				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileDataSplitType(DATA_AMOUNT_2, REMAIN_AMOUNT_1,
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(),
								seller));

				TradeMobileDataDefaultRequest request = new TradeMobileDataDefaultRequest(
						product.getId(), mobileData.getId(), DATA_AMOUNT_2);

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(buyer.getId());

				// when & then
				mockMvc.perform(post("/api/trades/mobile-data/default")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(
								ResultCode.INVALID_REMAIN_DATA_AMOUNT.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.INVALID_REMAIN_DATA_AMOUNT.getMessage()))
						.andDo(document(
								"trade/post-mobile-data-default-invalid-remain-data-amount-error",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("mobileDataId").description("데이터 아이디 (필수)"),
										fieldWithPath("dataAmount").description(
												"구매할 데이터양 (필수 X, 분할 구매는 필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}

		}
	}
}
