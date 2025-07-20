package com.dapanda.trade.controller;


import static com.dapanda.TestConstants.Member.CASH_5000;
import static com.dapanda.TestConstants.MobileData.DATA_AMOUNT_1;
import static com.dapanda.TestConstants.MobileData.DATA_AMOUNT_2;
import static com.dapanda.TestConstants.MobileData.PRICE_PER_100MB_150;
import static com.dapanda.TestConstants.MobileData.PRICE_PER_100MB_300;
import static com.dapanda.TestConstants.MobileData.REMAIN_AMOUNT_1;
import static com.dapanda.TestConstants.MobileData.REMAIN_AMOUNT_2;
import static com.dapanda.TestConstants.Plan.PROVIDING_DATA_AMOUNT_10;
import static com.dapanda.TestConstants.Product.PRICE_1500;
import static com.dapanda.TestConstants.Product.PRICE_3000;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.dapanda.trade.dto.MobileDataScrap;
import com.dapanda.trade.dto.request.TradeMobileDataDefaultRequest;
import com.dapanda.trade.dto.response.FindMobileDataScrapResponse;
import com.dapanda.trade.repository.TradeRepository;
import com.dapanda.trade.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
	@Autowired
	private PlanRepository planRepository;
	@Autowired
	private TradeService tradeService;

	private MockMvc mockMvc;

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
								PRICE_PER_100MB_300));
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
								PRICE_PER_100MB_300));
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
								PRICE_PER_100MB_300));
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
								PRICE_PER_100MB_300));
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
								PRICE_PER_100MB_300));
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
								PRICE_PER_100MB_300));
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

	@Nested
	@DisplayName("데이터 상품 자투리 조회 API")
	class FindMobileDataScrap {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 상품 자투리 조합이 존재할 때 조회를 성공한다")
			void findDataProductScrapWhenExist() throws Exception {

				// given
				Member seller1 = MemberFixture.createMember1();
				Member seller2 = MemberFixture.createMember2();
				List<Member> members = Arrays.asList(seller1, seller2);
				memberRepository.saveAll(members);

				MobileData mobileData1 = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_150);
				MobileData mobileData2 = MobileDataFixture.createMobileDataSplitType(DATA_AMOUNT_2,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				List<MobileData> mobileDataList = Arrays.asList(mobileData1, mobileData2);
				mobileDataRepository.saveAll(mobileDataList);

				Product product1 = ProductFixture.createMobileDataProduct(PRICE_1500,
						mobileData1.getId(), seller1);
				Product product2 = ProductFixture.createMobileDataProduct(PRICE_3000,
						mobileData2.getId(), seller2);
				List<Product> products = Arrays.asList(product1, product2);
				productRepository.saveAll(products);

				float dataAmount = DATA_AMOUNT_2;

				// when & then
				mockMvc.perform(get("/api/trades/mobile-data/scrap")
								.param("dataAmount", String.valueOf(dataAmount))
								.contentType(MediaType.APPLICATION_JSON)
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data").exists())
						.andDo(document("product/get-trades-mobile-data-scrap",
								queryParameters(
										parameterWithName("dataAmount").description(
												"구매할 데이터양 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.totalAmount").description("총 데이터양"),
										fieldWithPath("data.totalPrice").description("총 가격"),
										fieldWithPath("data.combinations").description("데이터 조합"),
										fieldWithPath("data.combinations[].productId").description(
												"상품 아이디"),
										fieldWithPath(
												"data.combinations[].mobileDataId").description(
												"데이터 가격"),
										fieldWithPath("data.combinations[].memberName").description(
												"판매자 이름"),
										fieldWithPath("data.combinations[].price").description(
												"상품 가격"),
										fieldWithPath(
												"data.combinations[].remainAmount").description(
												"남은 데이터양"),
										fieldWithPath(
												"data.combinations[].pricePer100MB").description(
												"100MB 당 가격").optional(),
										fieldWithPath("data.combinations[].splitType").description(
												"분할 타입 여부"),
										fieldWithPath("data.combinations[].updatedAt").description(
												"수정된 날짜")
								))
						);

				FindMobileDataScrapResponse response = tradeService.findMobileDataScrap(
						dataAmount);

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
			void findDataProductScrapWhenNotExist() throws Exception {

				// given
				float dataAmount = DATA_AMOUNT_2;

				// when & then
				mockMvc.perform(get("/api/trades/mobile-data/scrap")
								.param("dataAmount", String.valueOf(dataAmount))
								.contentType(MediaType.APPLICATION_JSON)
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data").exists())
						.andDo(document("product/get-trades-mobile-data-scrap",
								queryParameters(
										parameterWithName("dataAmount").description(
												"구매할 데이터양 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.totalAmount").description("총 데이터양"),
										fieldWithPath("data.totalPrice").description("총 가격"),
										fieldWithPath("data.combinations").description("데이터 조합")
								))
						);

				FindMobileDataScrapResponse response = tradeService.findMobileDataScrap(
						dataAmount);

				assertThat(response.getTotalAmount()).isEqualTo(0);
				assertThat(response.getTotalPrice()).isEqualTo(0); // 조합된 상품의 총 가격
				assertThat(response.getCombinations().size()).isEqualTo(0); // 조합된 상품 개수 확인
			}
		}
	}
}
