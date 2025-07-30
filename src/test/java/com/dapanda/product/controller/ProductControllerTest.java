package com.dapanda.product.controller;

import static com.dapanda.TestConstants.Member.USER_DETAILS_MEMBER_ID;
import static com.dapanda.TestConstants.MobileData.*;
import static com.dapanda.TestConstants.Pagination.DEFAULT_SIZE_2;
import static com.dapanda.TestConstants.Product.*;
import static com.dapanda.TestConstants.Wifi.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.*;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.plan.entity.*;
import com.dapanda.plan.repository.PlanRepository;
import com.dapanda.product.dto.request.*;
import com.dapanda.product.dto.response.*;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.*;
import com.dapanda.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("상품 컨트롤러 테스트")
class ProductControllerTest {

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
	private ProductImageRepository productImageRepository;
	@Autowired
	private PlanRepository planRepository;
	private MockMvc mockMvc;
	@Autowired
	private ProductService productService;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);

		cleanupDatabase();
	}

	@AfterEach
	void tearDown() {

		planRepository.deleteAll();
		productRepository.deleteAll();
		mobileDataRepository.deleteAll();
		memberRepository.deleteAll();
	}

	private void cleanupDatabase() {

		entityManager.clear();

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

		jdbcTemplate.execute("TRUNCATE TABLE wifi");
		jdbcTemplate.execute("TRUNCATE TABLE mobile_data");
		jdbcTemplate.execute("DELETE FROM product");
		jdbcTemplate.execute("DELETE FROM member");

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
	}

	@Nested
	@DisplayName("상품 등록 API")
	class CreateProduct {

		private CustomUserDetails userDetails;

		@BeforeEach
		void setUp() {

			Member member = memberRepository.save(MemberFixture.createMember1());
			userDetails = CustomUserDetails.from(member);
		}

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("정상적으로 모바일 데이터 상품을 등록한다")
			void createMobileData_success() throws Exception {

				Member member = memberRepository.save(Member.ofOAuthMember(
						"dummy0 + @email.com",
						"dummy1Name",
						OAuthProvider.KAKAO,
						MemberRole.ROLE_MEMBER));
				Plan plan = planRepository.save(
						Plan.of("청년 Value 베이직", new BigDecimal("30.0"), 15000,
								PlanCategory._5G, AgeGroup.YOUTH, member));

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				CreateMobileDataRequest request = new CreateMobileDataRequest(12000,
						new BigDecimal("2.0"), false);

				mockMvc.perform(MockMvcRequestBuilders.post("/api/products/mobile-data")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(0))
						.andExpect(jsonPath("$.message").value("정상 처리 되었습니다."))
						.andDo(document("product/post-mobile-data-success",
								requestFields(
										fieldWithPath("price").description("상품 가격"),
										fieldWithPath("dataAmount").description("데이터 용량(MB 단위)"),
										fieldWithPath("isSplitType").description("분할 판매 여부")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								)
						));
			}

			@Test
			@DisplayName("정상적으로 와이파이 상품을 등록한다")
			void createWifi_success() throws Exception {

				List<String> images = List.of(
						"이미지리스트1.jpg",
						"이미지리스트2.jpg"
				);

				CreateWifiRequest request = new CreateWifiRequest(
						15000,
						"Test Wifi",
						"설명",
						37.5,
						127.0,
						"서울특별시 강남구",
						LocalDateTime.now().plusMinutes(5),
						LocalDateTime.now().plusHours(2),
						images
				);

				mockMvc.perform(MockMvcRequestBuilders.post("/api/products/wifi")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(0))
						.andDo(document("product/post-wifi-success",
								requestFields(
										fieldWithPath("price").description("상품 가격"),
										fieldWithPath("title").description("와이파이 이름"),
										fieldWithPath("content").description("상세 설명"),
										fieldWithPath("latitude").description("위도"),
										fieldWithPath("longitude").description("경도"),
										fieldWithPath("address").description("주소"),
										fieldWithPath("startTime").description("시작 시간"),
										fieldWithPath("endTime").description("종료 시간"),
										fieldWithPath("images").description("이미지 Url 리스트")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								)
						));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("데이터 총합이 2GB 초과시 예외를 반환한다 - 모바일 데이터")
			void createMobileData_fail_overLimit() throws Exception {

				// 이미 sellingData가 꽉 찬 상태로 설정
				Member member = memberRepository.save(MemberFixture.createMember2());
				Plan plan = Plan.of(
						"청년 요금제",
						BigDecimal.valueOf(20),
						10000,
						PlanCategory._5G,
						AgeGroup.YOUTH,
						member
				);
				planRepository.save(plan);

				// sellingData 필드를 강제로 세팅하려면 set 메소드 또는 ReflectionTestUtils 사용
				MobileData fullMobileData = mobileDataRepository.save(
						MobileData.singleOf(BigDecimal.valueOf(20), 1000, false) // 2000MB짜리 상품
				);
				productRepository.save(
						Product.of(ProductState.ACTIVE, 1000, fullMobileData.getId(),
								ItemType.MOBILE_DATA, member)
				);

				userDetails = CustomUserDetails.from(member);

				CreateMobileDataRequest request = new CreateMobileDataRequest(12000,
						BigDecimal.valueOf(1.0), false);

				mockMvc.perform(MockMvcRequestBuilders.post("/api/products/mobile-data")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(3005))
						.andExpect(jsonPath("$.message").value("전송 가능한 데이터양을 초과했습니다."))
						.andDo(document("product/post-mobile-data-over-limit-error",
								requestFields(
										fieldWithPath("price").description("상품 가격"),
										fieldWithPath("dataAmount").description("데이터 용량(MB 단위)"),
										fieldWithPath("isSplitType").description("분할 판매 여부")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("에러 메시지")
								)
						));
			}

			@Test
			@DisplayName("필수 입력값이 누락되면 예외를 반환한다 - 모바일 데이터")
			void createMobileData_fail_missingField() throws Exception {

				CreateMobileDataRequest incompleteRequest = new CreateMobileDataRequest(
						null,     // price 누락
						new BigDecimal("2.0"),
						false
				);

				java.util.Map<String, Object> map = objectMapper.convertValue(incompleteRequest,
						java.util.Map.class);
				map.remove("price");

				String invalidJson = objectMapper.writeValueAsString(map);

				mockMvc.perform(MockMvcRequestBuilders.post("/api/products/mobile-data")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON)
								.content(invalidJson))
						.andExpect(status().isBadRequest())
						.andDo(document("product/post-mobile-data-missing-field-error",
								requestFields(
										fieldWithPath("dataAmount").description("데이터 용량(MB 단위)"),
										fieldWithPath("isSplitType").description("분할 판매 여부")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("에러 메시지")
								)
						));
			}

			@Test
			@DisplayName("필수 입력값이 누락되면 예외를 반환한다 - 와이파이")
			void createWifi_fail_missingField() throws Exception {

				List<String> images = List.of(
						"이미지리스트1.jpg",
						"이미지리스트2.jpg"
				);

				CreateWifiRequest incompleteRequest = new CreateWifiRequest(
						null,
						null,
						null,
						37.5,
						127.0,
						"서울특별시 강남구",
						LocalDateTime.parse("2025-07-18T10:00:00"),
						LocalDateTime.parse("2025-07-18T20:00:00"),
						images
				);

				ObjectMapper mapper = objectMapper;
				java.util.Map<String, Object> map = mapper.convertValue(incompleteRequest,
						java.util.Map.class);

				map.remove("title");
				map.remove("content");
				map.remove("price");

				String invalidJson = mapper.writeValueAsString(map);

				mockMvc.perform(MockMvcRequestBuilders.post("/api/products/wifi")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON)
								.content(invalidJson))
						.andExpect(status().isBadRequest())
						.andDo(document("product/post-wifi-missing-field-error",
								requestFields(
										fieldWithPath("latitude").description("위도"),
										fieldWithPath("longitude").description("경도"),
										fieldWithPath("address").description("주소"),
										fieldWithPath("startTime").description("시작 시간"),
										fieldWithPath("endTime").description("종료 시간"),
										fieldWithPath("images").description("이미지 Url 리스트")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("에러 메시지")
								)
						));
			}
		}
	}


	@Nested
	@DisplayName("데이터 상품 목록 조회 API")
	class MobileDataList {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 상품 목록을 조회한다")
			void getMobileDataByCursorTest() throws Exception {

				// given
				int size = 2;
				String productSortOption = "RECENT";
				BigDecimal dataAmount = new BigDecimal("2.0");

				Member member = memberRepository.save(MemberFixture.createMember1());

				MobileData mobileData1 = MobileDataFixture.createMobileData(BigDecimal.valueOf(2.0),
						BigDecimal.valueOf(2.0), 500);
				MobileData mobileData2 = MobileDataFixture.createMobileData(BigDecimal.valueOf(2.0),
						BigDecimal.valueOf(2.0), 600);
				MobileData mobileData3 = MobileDataFixture.createMobileData(BigDecimal.valueOf(3.0),
						BigDecimal.valueOf(3.0), 700);
				mobileDataRepository.saveAll(List.of(mobileData1, mobileData2, mobileData3));

				Product product1 = ProductFixture.createMobileDataProduct(3000,
						mobileData1.getId(), member);
				Product product2 = ProductFixture.createMobileDataProduct(4000,
						mobileData2.getId(), member);
				Product product3 = ProductFixture.createMobileDataProduct(5000,
						mobileData3.getId(), member);
				productRepository.saveAll(List.of(product1, product2, product3));

				// when & then
				mockMvc.perform(
								MockMvcRequestBuilders.get(
												"/api/products/mobile-data")
										.param("size", String.valueOf(size))
										.param("productSortOption", productSortOption)
										.param("dataAmount", String.valueOf(dataAmount))
										.contentType(MediaType.APPLICATION_JSON)
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data").exists())
						.andDo(document("product/get-products-mobile-data",
								queryParameters(
										parameterWithName("cursorId").description(
												"마지막 커서 아이디 (선택)").optional(),
										parameterWithName("size").description(
												"페이지 사이즈 (필수, 1 이상 정수)"),
										parameterWithName("productSortOption").description(
												"정렬 조건 (필수, 기본값: 최신순) - RECENT(최신순), PRICE_ASC(가격 낮은순), AMOUNT_ASC(데이터 용량 적은순), AMOUNT_DESC(데이터 용량 많은순"),
										parameterWithName("dataAmount").description("데이터 양 (선택)")
												.optional()
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.data").description("상품 데이터 배열"),
										fieldWithPath("data.data[].productId").description(
												"상품 아이디"),
										fieldWithPath("data.data[].price").description(
												"상품 가격"),
										fieldWithPath("data.data[].itemId").description(
												"모바일 데이터 아이디"),
										fieldWithPath("data.data[].memberName").description(
												"상품을 등록한 회원 이름"),
										fieldWithPath("data.data[].profileImageUrl").description(
												"상품을 등록한 회원의 프로필 이미지 URL"),
										fieldWithPath("data.data[].remainAmount").description(
												"데이터 잔여량"),
										fieldWithPath("data.data[].pricePer100MB").description(
												"100MB당 가격"),
										fieldWithPath("data.data[].splitType").description(
												"분할 판매 여부"),
										fieldWithPath("data.data[].updatedAt").description(
												"수정된 날짜"),
										fieldWithPath("data.pageInfo.nextCursorId").description(
												"다음 커서 아이디"),
										fieldWithPath("data.pageInfo.hasNext").description(
												"다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.size").description("페이지 크기")
								))
						);
			}

			@Nested
			@DisplayName("실패 케이스")
			class Fail {

				@Test
				@DisplayName("데이터 상품 목록 조회 시 size가 null이거나 1 미만이면 예외를 던진다")
				void throwExceptionWhenSizeIsNullOrLessThan1() throws Exception {

					// given & when & then
					mockMvc.perform(MockMvcRequestBuilders.get("/api/products/mobile-data")
									.param("cursorId", "3")
									.param("size", "0")
									.param("productSortOption", "RECENT")
									.param("dataAmount", String.valueOf(2.0F))
									.contentType(MediaType.APPLICATION_JSON)
							)
							.andExpect(status().isBadRequest())
							.andDo(document(
									"product/get-products-mobile-data-size-validation-error"));
				}

				@Test
				@DisplayName("데이터 상품 목록 조회 시 상품 정렬 조건이 유효하지 않으면 예외를 던진다")
				void throwExceptionWhenProductSortOptionIsInvalid() throws Exception {

					// given & when & then
					mockMvc.perform(MockMvcRequestBuilders.get(
											"/api/products/mobile-data")
									.param("cursorId", "3")
									.param("size", String.valueOf(2))
									.param("productSortOption", "RECENT123")
									.param("dataAmount", String.valueOf(2.0F))
									.contentType(MediaType.APPLICATION_JSON)
							)
							.andExpect(status().isBadRequest())
							.andExpect(jsonPath("$.message").value(
									containsString("유효하지 않은 상품 정렬 조건입니다")))
							.andExpect(jsonPath("$.code").value(3000))
							.andDo(document(
									"product/get-products-mobile-data-plan-sort-option-validation-error"));
				}
			}
		}
	}

	@Nested
	@DisplayName("와이파이 상품 목록 조회 API")
	class WifiList {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("와이파이 상품 목록을 조회한다")
			void getWifiByCursorTest() throws Exception {

				// given
				Long cursorId = 1L;
				int size = 2;
				String productSortOption = "DISTANCE_ASC";
				boolean isOpen = false;
				double latitude = 30.0;
				double longitude = 127.0;

				Member member = memberRepository.save(MemberFixture.createMember1());

				Wifi wifi1 = WifiFixture.createWifi("제목1", "내용1", 30.0, 126.0, ADDRESS,
						LocalDateTime.of(2025, 7, 14, 10, 0),
						LocalDateTime.of(2025, 7, 14, 18, 0));
				Wifi wifi2 = WifiFixture.createWifi("제목2", "내용2", 30.0, 126.0, ADDRESS,
						LocalDateTime.of(2025, 7, 14, 10, 0),
						LocalDateTime.of(2025, 7, 14, 18, 0));
				Wifi wifi3 = WifiFixture.createWifi("제목3", "내용3", 30.0, 126.0, ADDRESS,
						LocalDateTime.of(2025, 7, 14, 10, 0),
						LocalDateTime.of(2025, 7, 14, 18, 0));
				wifiRepository.saveAll(List.of(wifi1, wifi2, wifi3));

				ProductImage productImage1 = ProductImageFixture.createProductImage("imageUrl1", 1,
						wifi1.getId());
				ProductImage productImage2 = ProductImageFixture.createProductImage("imageUrl2", 2,
						wifi2.getId());
				productImageRepository.saveAll(List.of(productImage1, productImage2));

				Product product1 = ProductFixture.createWifiProduct(3000, wifi1.getId(), member);
				Product product2 = ProductFixture.createWifiProduct(4000, wifi2.getId(), member);
				Product product3 = ProductFixture.createWifiProduct(5000, wifi3.getId(), member);
				productRepository.saveAll(List.of(product1, product2, product3));

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(MockMvcRequestBuilders.get("/api/products/wifi")
								.param("cursorId", String.valueOf(cursorId))
								.param("size", String.valueOf(size))
								.param("productSortOption", productSortOption)
								.param("open", String.valueOf(isOpen))
								.param("latitude", String.valueOf(latitude))
								.param("longitude", String.valueOf(longitude))
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data").exists())
						.andDo(document("product/get-products-wifi",
								queryParameters(
										parameterWithName("cursorId").description(
												"마지막 커서 아이디 (선택)"),
										parameterWithName("size").description(
												"페이지 사이즈 (필수, 1 이상 정수)"),
										parameterWithName("productSortOption").description(
												"정렬 조건 (필수) - PRICE_ASC(가격 낮은순), AVERAGE_RATE_DESC(평점 높은순), DISTANCE_ASC(거리 가까운순)"),
										parameterWithName("open").description("영업중 여부 (선택)"),
										parameterWithName("latitude").description("사용자의 위도 (필수)"),
										parameterWithName("longitude").description("사용자의 경도 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.data").description("상품 데이터 배열"),
										fieldWithPath("data.data[].productId").description(
												"상품 아이디"),
										fieldWithPath("data.data[].price").description(
												"상품 가격"),
										fieldWithPath("data.data[].itemId").description(
												"와이파이 아이디"),
										fieldWithPath("data.data[].memberName").description(
												"상품을 등록한 회원 이름"),
										fieldWithPath("data.data[].profileImageUrl").description(
												"상품을 등록한 회원의 프로필 이미지 URL"),
										fieldWithPath("data.data[].title").description("게시물 제목"),
										fieldWithPath("data.data[].imageUrl").description(
												"대표 이미지 URL").optional(),
										fieldWithPath("data.data[].latitude").description("위도"),
										fieldWithPath("data.data[].longitude").description("경도"),
										fieldWithPath("data.data[].address").description("주소"),
										fieldWithPath("data.data[].averageRate").description(
												"평균 평점"),
										fieldWithPath("data.data[].distanceKm").description(
												"현 위치로부터 거리 (km)"),
										fieldWithPath("data.data[].open").description("영업중 여부"),
										fieldWithPath("data.data[].startTime").description(
												"시작 시간"),
										fieldWithPath("data.data[].endTime").description(
												"종료 시간"),
										fieldWithPath("data.pageInfo.nextCursorId").description(
												"다음 커서 아이디"),
										fieldWithPath("data.pageInfo.hasNext").description(
												"다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.size").description("페이지 크기")
								))
						);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("와이파이 상품 목록 조회 시 size가 null이거나 1 미만이면 예외를 던진다")
			void throwExceptionWhenSizeIsNullOrLessThan1() throws Exception {

				// given & when & then
				mockMvc.perform(MockMvcRequestBuilders.get("/api/products/wifi")
								.param("cursorId", "3")
								.param("size", String.valueOf(0))
								.param("productSortOption", "RECENT123")
								.param("isOpen", String.valueOf(true))
								.param("latitude", String.valueOf(30.0))
								.param("longitude", String.valueOf(126.0))
								.contentType(MediaType.APPLICATION_JSON)
						)
						.andExpect(status().isBadRequest())
						.andDo(document(
								"product/get-products-wifi-size-validation-error"));
			}

			@Test
			@DisplayName("와이파이 상품 목록 조회 시 위도, 경도 값이 유효하지 않으면 예외를 던진다")
			void throwExceptionWhenProductSortOptionIsInvalid() throws Exception {

				// given & when & then
				mockMvc.perform(MockMvcRequestBuilders.get("/api/products/wifi")
								.contentType(MediaType.APPLICATION_JSON)
								.param("cursorId", "3")
								.param("size", String.valueOf(2))
								.param("productSortOption", "RECENT123")
								.param("isOpen", String.valueOf(true))
						)
						.andExpect(status().isBadRequest())
						.andDo(document(
								"product/get-products-wifi-plan-sort-option-validation-error"));
			}
		}
	}

	@Nested
	@DisplayName("데이터 상품 상세 조회 API")
	class MobileDataInfo {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 상품을 상세 조회한다")
			void getMobileDataInfo() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());

				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(DATA_AMOUNT_1, REMAIN_AMOUNT_1,
								PRICE_PER_100MB_300));

				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(),
								member));

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/products/mobile-data/{productId}", product.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.data.productId").value(product.getId()))
						.andExpect(jsonPath("$.data.itemId").value(mobileData.getId()))
						.andExpect(jsonPath("$.data.price").value(PRICE_3000))
						.andExpect(jsonPath("$.data.memberId").value(member.getId()))
						.andExpect(jsonPath("$.data.memberName").value(member.getName()))
						.andExpect(jsonPath("$.data.profileImageUrl").value(
								member.getProfileImageUrl()))
						.andExpect(jsonPath("$.data.remainAmount").value(REMAIN_AMOUNT_1))
						.andExpect(jsonPath("$.data.pricePer100MB").value(PRICE_PER_100MB_300))
						.andExpect(jsonPath("$.data.averageRate").exists())
						.andExpect(jsonPath("$.data.reviewCount").exists())
						.andExpect(jsonPath("$.data.myProduct").exists())
						.andExpect(jsonPath("$.data.splitType").exists())
						.andExpect(jsonPath("$.data.updatedAt").exists())
						.andDo(document("product/get-mobile-data-info",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data.productId").description("상품 아이디"),
										fieldWithPath("data.itemId").description("데이터 아이디"),
										fieldWithPath("data.price").description("가격"),
										fieldWithPath("data.memberId").description(
												"상품을 등록한 회원의 아이디"),
										fieldWithPath("data.memberName").description(
												"상품을 등록한 회원의 이름"),
										fieldWithPath("data.profileImageUrl").description(
												"상품을 등록한 회원의 프로필 이미지 URL"),
										fieldWithPath("data.remainAmount").description("남은 데이터양"),
										fieldWithPath("data.pricePer100MB").description(
												"100MB 당 가격"),
										fieldWithPath("data.averageRate").description("평균 별점"),
										fieldWithPath("data.reviewCount").description("리뷰 수"),
										fieldWithPath("data.myProduct").description("자신이 등록한 글 여부"),
										fieldWithPath("data.splitType").description("분할 여부"),
										fieldWithPath("data.updatedAt").description("수정된 시간")
								))
						);

				MobileDataInfoResponse actualResponse = productService.findMobileDataInfo(
						product.getId(), member.getId());

				assertThat(actualResponse.getProductId()).isEqualTo(product.getId());
				assertThat(actualResponse.getItemId()).isEqualTo(mobileData.getId());
				assertThat(actualResponse.getRemainAmount()).isEqualByComparingTo(REMAIN_AMOUNT_1);
				assertThat(actualResponse.getPricePer100MB()).isEqualTo(PRICE_PER_100MB_300);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("데이터 상품 상세 조회 시 상품 아이디가 존재하지 않으면 예외를 던진다")
			void throwsExceptionWhenProductIdNotExist() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/products/mobile-data/{productId}", INVALID_PRODUCT_ID)
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isBadRequest())
						.andDo(document("product/get-mobile-data-info-not-exist-product-id-error",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}

			@Test
			@DisplayName("데이터 상품 상세 조회 시 상품이 유효하지 않으면 예외를 던진다")
			void throwsExceptionWhenProductInvalid() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());

				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(DATA_AMOUNT_1, REMAIN_AMOUNT_1,
								PRICE_PER_100MB_300));

				Product product = productRepository.save(
						ProductFixture.createMobileDataProductInactive(PRICE_3000,
								mobileData.getId(),
								member));

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/products/mobile-data/{productId}", product.getId() + 1)
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isBadRequest())
						.andDo(document("product/get-mobile-data-info-invalid-product-error",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}
		}
	}

	@Nested
	@DisplayName("와이파이 상품 상세 조회 API")
	class WifiInfo {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("와이파이 상품을 상세 조회한다")
			void getWifiInfo() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());

				Wifi wifi = wifiRepository.save(
						WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
								START_DATETIME, END_DATETIME));

				Product product = productRepository.save(
						ProductFixture.createWifiProduct(PRICE_3000, wifi.getId(), member));

				productImageRepository.save(
						ProductImageFixture.createProductImage(IMAGE_URL_1, 3, wifi.getId()));
				productImageRepository.save(
						ProductImageFixture.createProductImage(IMAGE_URL_2, 2, wifi.getId()));

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/products/wifi/{productId}", product.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.data.productId").value(product.getId()))
						.andExpect(jsonPath("$.data.itemId").value(wifi.getId()))
						.andExpect(jsonPath("$.data.price").value(PRICE_3000))
						.andExpect(jsonPath("$.data.memberId").value(member.getId()))
						.andExpect(jsonPath("$.data.memberName").value(member.getName()))
						.andExpect(jsonPath("$.data.profileImageUrl").value(
								member.getProfileImageUrl()))
						.andExpect(jsonPath("$.data.title").value(TITLE))
						.andExpect(jsonPath("$.data.content").value(CONTENT))
						.andExpect(jsonPath("$.data.latitude").value(LATITUDE))
						.andExpect(jsonPath("$.data.longitude").value(LONGITUDE))
						.andExpect(jsonPath("$.data.address").value(ADDRESS))
						.andExpect(jsonPath("$.data.content").value(CONTENT))
						.andExpect(jsonPath("$.data.averageRate").exists())
						.andExpect(jsonPath("$.data.reviewCount").exists())
						.andExpect(jsonPath("$.data.myProduct").exists())
						.andExpect(jsonPath("$.data.imageUrls").exists())
						.andExpect(jsonPath("$.data.startTime").exists())
						.andExpect(jsonPath("$.data.endTime").exists())
						.andExpect(jsonPath("$.data.open").exists())
						.andExpect(jsonPath("$.data.updatedAt").exists())
						.andDo(document("product/get-wifi-info",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data.productId").description("상품 아이디"),
										fieldWithPath("data.itemId").description("데이터 아이디"),
										fieldWithPath("data.price").description("가격"),
										fieldWithPath("data.memberId").description(
												"상품을 등록한 회원의 아이디"),
										fieldWithPath("data.memberName").description(
												"상품을 등록한 회원의 이름"),
										fieldWithPath("data.profileImageUrl").description(
												"상품을 등록한 회원의 프로필 이미지 URL"),
										fieldWithPath("data.title").description("게시물 제목"),
										fieldWithPath("data.content").description("게시물 내용"),
										fieldWithPath("data.latitude").description("위도"),
										fieldWithPath("data.longitude").description("경도"),
										fieldWithPath("data.address").description("주소"),
										fieldWithPath("data.averageRate").description("평균 별점"),
										fieldWithPath("data.reviewCount").description("리뷰 수"),
										fieldWithPath("data.myProduct").description("자신이 등록한 글 여부"),
										fieldWithPath("data.imageUrls[]").description(
												"이미지 URL (우선순위 높은순)"),
										fieldWithPath("data.startTime").description("시작 시간"),
										fieldWithPath("data.endTime").description("종료 시간"),
										fieldWithPath("data.open").description("영업중 여부"),
										fieldWithPath("data.updatedAt").description("수정된 시간")
								))
						);

				WifiInfoResponse actualResponse = productService.findWifiInfo(product.getId(),
						member.getId());

				assertThat(actualResponse.getProductId()).isEqualTo(product.getId());
				assertThat(actualResponse.getItemId()).isEqualTo(wifi.getId());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("와이파이 상품 상세 조회 시 상품 아이디가 존재하지 않으면 예외를 던진다")
			void throwsExceptionWhenProductIdNotExist() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/products/wifi/{productId}", INVALID_PRODUCT_ID)
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isBadRequest())
						.andDo(document("product/get-wifi-info-not-exist-product-id-error",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}

			@Test
			@DisplayName("와이파이 상품 상세 조회 시 상품이 유효하지 않으면 예외를 던진다")
			void throwsExceptionWhenProductInvalid() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());

				Wifi wifi = wifiRepository.save(
						WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE,
								ADDRESS, START_DATETIME, END_DATETIME));

				Product product = productRepository.save(
						ProductFixture.createWifiProductInactive(PRICE_3000, wifi.getId(), member));

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/products/wifi/{productId}", product.getId() + 1)
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isBadRequest())
						.andDo(document("product/get-wifi-info-invalid-product-error",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}
		}
	}

	@Nested
	@DisplayName("데이터 상품 수정 API")
	class UpdateMobileData {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 상품을 수정한다")
			void updateMobileDataInfo() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember2());
				Plan plan = Plan.of(
						"청년 요금제2",
						BigDecimal.valueOf(15),
						10000,
						PlanCategory._5G,
						AgeGroup.YOUTH,
						member
				);
				planRepository.save(plan);

				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT, BEFORE_REMAIN_AMOUNT,
								PRICE_PER_100MB_300));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(),
								member));

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				UpdateMobileDataRequest request = new UpdateMobileDataRequest(product.getId(),
						NEW_PRICE_9000, CHANGED_AMOUNT, SPLIT_TYPE);

				// when & then
				mockMvc.perform(put("/api/products/mobile-data")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.data.productId").value(product.getId()))
						.andDo(document("product/put-mobile-data",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("price").description("상품 가격 (필수)"),
										fieldWithPath("changedAmount").description(
												"상품 데이터양 (필수)"),
										fieldWithPath("isSplitType").description("분할 여부 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data.productId").description("상품 아이디")
								))
						);

				Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
				MobileData updatedMobileData = mobileDataRepository.findById(mobileData.getId())
						.orElseThrow();

				assertThat(updatedProduct.getId()).isEqualTo(product.getId());
				assertThat(updatedProduct.getPrice()).isEqualTo(NEW_PRICE_9000);
				assertThat(updatedMobileData.getDataAmount()).isEqualByComparingTo(CHANGED_AMOUNT);
				assertThat(updatedMobileData.getRemainAmount()).isEqualByComparingTo(
						CHANGED_AMOUNT);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("상품 등록자가 아닌 회원이 상품을 수정하면 예외를 던진다")
			public void failUpdateMobileDataIfMemberIsWrongTest() throws Exception {

				// given
				Member member1 = memberRepository.save(MemberFixture.createMember1());
				Member member2 = memberRepository.save(MemberFixture.createMember2());
				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT, BEFORE_REMAIN_AMOUNT,
								PRICE_PER_100MB_300));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(),
								member1));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member2.getId());

				UpdateMobileDataRequest request = new UpdateMobileDataRequest(product.getId(),
						NEW_PRICE_9000, CHANGED_AMOUNT, SPLIT_TYPE);

				// when & then
				mockMvc.perform(put("/api/products/mobile-data")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("product/put-mobile-data-invalid-member-error",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("price").description("상품 가격 (필수)"),
										fieldWithPath("changedAmount").description(
												"데이터 변화량 (필수, 음수/양수)"),
										fieldWithPath("isSplitType").description("분할 여부 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}

			@Test
			@DisplayName("데이터 전송량과 판매한 데이터의 합이 데이터 전송 정책을 초과하면 예외를 던진다")
			public void failUpdateMobileDataIfDataTransferPolicyTest() throws Exception {

				// given
				Member member = memberRepository.save(
						MemberFixture.createMemberWithSellingData(SELLING_DATA));
				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT, BEFORE_REMAIN_AMOUNT,
								PRICE_PER_100MB_300));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(),
								member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				UpdateMobileDataRequest request = new UpdateMobileDataRequest(product.getId(),
						NEW_PRICE_9000, CHANGED_AMOUNT, SPLIT_TYPE);

				// when & then
				mockMvc.perform(put("/api/products/mobile-data")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("product/put-mobile-data-invalid-data-amount-policy-error",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("price").description("상품 가격 (필수)"),
										fieldWithPath("changedAmount").description(
												"데이터 변화량 (필수, 음수/양수)"),
										fieldWithPath("isSplitType").description("분할 여부 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}

			@Test
			@DisplayName("데이터 전송량이 유효하지 않을 때 예외를 던진다")
			public void failUpdateMobileDataIfDataInvalidTest() throws Exception {

				// given
				Member member = memberRepository.save(
						MemberFixture.createMemberWithSellingData(SELLING_DATA));
				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT, BEFORE_REMAIN_AMOUNT,
								PRICE_PER_100MB_300));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(),
								member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				UpdateMobileDataRequest request = new UpdateMobileDataRequest(product.getId(),
						NEW_PRICE_9000, EXCEED_CHANGED_AMOUNT, SPLIT_TYPE);

				// when & then
				mockMvc.perform(put("/api/products/mobile-data")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("product/put-mobile-data-exceed-data-amount-error",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("price").description("상품 가격 (필수)"),
										fieldWithPath("changedAmount").description(
												"데이터 변화량 (필수, 음수/양수)"),
										fieldWithPath("isSplitType").description("분할 여부 (필수)")
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
	@DisplayName("와이파이 상품 수정 API")
	class UpdateWifi {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("와이파이 상품을 수정한다")
			void updateWifiInfo() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				Wifi wifi = wifiRepository.save(
						WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
								START_DATETIME, END_DATETIME));
				Product product = productRepository.save(
						ProductFixture.createWifiProduct(PRICE_3000, wifi.getId(), member));

				List<String> imageUrls = List.of("image1.jpg", "image2.jpg", "image3.jpg");

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				UpdateWifiRequest request = new UpdateWifiRequest(product.getId(), NEW_PRICE_9000,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						ADDRESS, imageUrls, START_DATETIME, END_DATETIME);

				// when & then
				mockMvc.perform(put("/api/products/wifi")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.data.productId").value(product.getId()))
						.andDo(document("product/put-wifi",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("price").description("상품 가격 (필수)"),
										fieldWithPath("title").description("상품 제목 (필수)"),
										fieldWithPath("content").description("상품 본문 (필수)"),
										fieldWithPath("latitude").description("위도 (필수)"),
										fieldWithPath("longitude").description("경도 (필수)"),
										fieldWithPath("imageUrls").description("이미지 (필수)"),
										fieldWithPath("address").description("지도 (필수)"),
										fieldWithPath("startTime").description("시작 시간 (필수)"),
										fieldWithPath("endTime").description("종료 시간 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data.productId").description("상품 아이디")
								))
						);

				Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
				Wifi updatedWifi = wifiRepository.findById(wifi.getId())
						.orElseThrow();

				assertThat(updatedProduct.getId()).isEqualTo(product.getId());
				assertThat(updatedProduct.getPrice()).isEqualTo(NEW_PRICE_9000);
				assertThat(updatedWifi.getTitle()).isEqualTo(CHANGED_TITLE);
				assertThat(updatedWifi.getContent()).isEqualTo(CHANGED_CONTENT);
				assertThat(updatedWifi.getLatitude()).isEqualTo(CHANGED_LATITUDE);
				assertThat(updatedWifi.getLongitude()).isEqualTo(CHANGED_LONGITUDE);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("상품 등록자가 아닌 회원이 상품을 수정하면 예외를 던진다")
			public void failUpdateMobileDataIfMemberIsWrongTest() throws Exception {

				// given
				Member member1 = memberRepository.save(MemberFixture.createMember1());
				Member member2 = memberRepository.save(MemberFixture.createMember2());
				Wifi wifi = wifiRepository.save(
						WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
								START_DATETIME, END_DATETIME));
				Product product = productRepository.save(
						ProductFixture.createWifiProduct(PRICE_3000, wifi.getId(), member1));
				List<String> imageUrls = List.of("image1.jpg", "image2.jpg", "image3.jpg");

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member2.getId());

				UpdateWifiRequest request = new UpdateWifiRequest(product.getId(), NEW_PRICE_9000,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						ADDRESS, imageUrls, START_DATETIME, END_DATETIME);

				// when & then
				mockMvc.perform(put("/api/products/wifi")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("product/put-wifi-invalid-member-error",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("price").description("상품 가격 (필수)"),
										fieldWithPath("title").description("상품 제목 (필수)"),
										fieldWithPath("content").description("상품 본문 (필수)"),
										fieldWithPath("latitude").description("위도 (필수)"),
										fieldWithPath("longitude").description("경도 (필수)"),
										fieldWithPath("address").description("주소 (필수)"),
										fieldWithPath("imageUrls").description("이미지 (필수)"),
										fieldWithPath("startTime").description("시작 시간 (필수)"),
										fieldWithPath("endTime").description("종료 시간 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}

			@Test
			@DisplayName("종료 시간이 시작 시간보다 늦으면 예외를 던진다")
			public void failUpdateWifiIfTimeIsInvalidTest() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				Wifi wifi = wifiRepository.save(
						WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
								START_DATETIME, END_DATETIME));
				Product product = productRepository.save(
						ProductFixture.createWifiProduct(PRICE_3000, wifi.getId(), member));
				List<String> imageUrls = List.of("image1.jpg", "image2.jpg", "image3.jpg");

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				UpdateWifiRequest request = new UpdateWifiRequest(product.getId(), NEW_PRICE_9000,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						ADDRESS, imageUrls, WRONG_START_DATETIME, WRONG_END_DATETIME);

				// when & then
				mockMvc.perform(put("/api/products/wifi")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("product/put-wifi-invalid-time-error",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("price").description("상품 가격 (필수)"),
										fieldWithPath("title").description("상품 제목 (필수)"),
										fieldWithPath("content").description("상품 본문 (필수)"),
										fieldWithPath("latitude").description("위도 (필수)"),
										fieldWithPath("longitude").description("경도 (필수)"),
										fieldWithPath("address").description("주소 (필수)"),
										fieldWithPath("imageUrls").description("이미지 (필수)"),
										fieldWithPath("startTime").description("시작 시간 (필수)"),
										fieldWithPath("endTime").description("종료 시간 (필수)")
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
	@DisplayName("상품 삭제 API")
	class DeleteProduct {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("상품을 삭제한다")
			void deleteProduct() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				Plan plan = Plan.of(
						"청년 요금제",
						BigDecimal.valueOf(10),
						10000,
						PlanCategory._5G,
						AgeGroup.YOUTH,
						member
				);
				planRepository.save(plan);

				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(DATA_AMOUNT_1, REMAIN_AMOUNT_1,
								PRICE_PER_100MB_300));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(),
								member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				// when & then
				mockMvc.perform(delete("/api/products/{productId}", product.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isOk())
						.andDo(document("product/delete-product",
								pathParameters(
										parameterWithName("productId").description(
												"삭제할 상품 아이디 (필수)")
								)
						));

				Product deletedProduct = productRepository.findById(product.getId()).orElseThrow();

				assertThat(deletedProduct.getState()).isEqualTo(ProductState.DELETED);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("존재하지 않는 상품이면 예외를 던진다")
			void deleteProductFailWhenNotFoundProductTest() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(DATA_AMOUNT_1,
								REMAIN_AMOUNT_1, PRICE_PER_100MB_300));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(),
								member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				// when & then
				mockMvc.perform(delete("/api/products/{productId}", INVALID_PRODUCT_ID)
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("product/delete-product-not-found-error",
								pathParameters(
										parameterWithName("productId").description(
												"삭제할 상품 아이디 (필수)")
								)
						));
			}

			@Test
			@DisplayName("이미 삭제된 상품이면 예외를 던진다")
			void deleteProductFailWhenAlreadyDeletedTest() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(DATA_AMOUNT_1,
								REMAIN_AMOUNT_1, PRICE_PER_100MB_300));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProductWithIdWithState(null,
								mobileData.getId(), ProductState.DELETED, member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				// when & then
				mockMvc.perform(delete("/api/products/{productId}", PRODUCT_ID)
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("product/delete-product-already-deleted-error",
								pathParameters(
										parameterWithName("productId").description(
												"삭제할 상품 아이디 (필수)")
								)
						));
			}
		}
	}

	@Nested
	@DisplayName("판매 상품 조회 API")
	class ReadSellingProduct {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("조회할 회원의 아이디와 상품의 상태가 ACTIVE 인 판매 상품을 조회한다")
			public void readSellingProductTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());

				List<MobileData> mobileDataList = mobileDataRepository.saveAll(
						MobileDataFixture.createMobileDataList());

				List<Product> productActivList = productRepository.saveAll(
						ProductFixture.createProductList(seller, mobileDataList,
								ProductState.ACTIVE));
				productRepository.saveAll(ProductFixture.createProductList(seller, mobileDataList,
						ProductState.SOLD_OUT));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(USER_DETAILS_MEMBER_ID);

				//when & then
				mockMvc.perform(get("/api/members/{memberId}/selling-products", seller.getId())
								.param("productState", ProductState.ACTIVE.name())
								.param("size", String.valueOf(DEFAULT_SIZE_2))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").exists())
						.andExpect(jsonPath("$.data.data.length()").value(
								Math.min(DEFAULT_SIZE_2, productActivList.size())))
						.andExpect(jsonPath("$.data.pageInfo").exists())
						.andExpect(jsonPath("$.data.pageInfo.hasNext").value(true))
						.andExpect(jsonPath("$.data.pageInfo.size").value(DEFAULT_SIZE_2))
						.andExpect(jsonPath("$.data.pageInfo.nextCursorId").exists())
						.andDo(document("product/read-selling-product",
								pathParameters(
										parameterWithName("memberId").description(
												"조회할 회원의 아이디 (필수)")
								),
								queryParameters(
										parameterWithName("productState").description(
												"조회할 판매 상품의 상태 (필수) (ACTIVE / SOLD_OUT)"),
										parameterWithName("cursorId").description("커서 아이디 (선택)")
												.optional(),
										parameterWithName("size").description(
												"페이지 크기 (선택, 기본값 = 2, 최대 = 100)").optional()
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										fieldWithPath("data").description("페이징 처리된 리뷰 데이터"),
										fieldWithPath("data.data[]").description("조회된 판매 상품 목록"),
										fieldWithPath("data.data[].productId").description(
												"상품 아이디"),
										fieldWithPath("data.data[].type").description("상품 타입"),
										fieldWithPath("data.data[].state").description("상품 상태"),
										fieldWithPath("data.data[].dataAmount").description(
														"모바일 데이터 전체량").type(JsonFieldType.NUMBER)
												.optional(),
										fieldWithPath("data.data[].remainAmount").description(
												"모바일 데이터 잔량").type(JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.data[].startTime").description(
														"와이파이 판매 시작 시간").type(JsonFieldType.NUMBER)
												.optional(),
										fieldWithPath("data.data[].endTime").description(
												"와이파이 판매 종료 시간").type(
												JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.data[].createdAt").description(
												"상품 등록 시간"),
										fieldWithPath("data.data[].updatedAt").description(
												"상품 수정 시간"),
										fieldWithPath("data.pageInfo").description("페이지 정보"),
										fieldWithPath("data.pageInfo.size").description(
												"현재 페이지 크기"),
										fieldWithPath("data.pageInfo.hasNext").description(
												"다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.nextCursorId").description(
												"다음 페이지 조회 시 사용할 커서 아이디 (다음 페이지가 없으면 null)"),
										fieldWithPath("data.count").description("조회된 전체 갯수")
								)
						));
			}
		}
	}

	@Nested
	@DisplayName("판매 시세 조회 API")
	class FindMarketPrice {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("판매 시세(최근거래가, 평균거래가)를 조회한다")
			public void findMarketPrice() throws Exception {

				// given
				Member seller = MemberFixture.createMember1();
				memberRepository.save(seller);

				MobileData mobile1 = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				MobileData mobile2 = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_150);
				mobileDataRepository.saveAll(new ArrayList<>(Arrays.asList(mobile1, mobile2)));

				Product product1 = ProductFixture.createMobileDataProductInactive(PRICE_3000,
						mobile1.getId(), seller);
				Product product2 = ProductFixture.createMobileDataProductInactive(PRICE_1500,
						mobile2.getId(), seller);
				productRepository.saveAll(new ArrayList<>(Arrays.asList(product1, product2)));

				// when & then
				mockMvc.perform(get("/api/products/market-price")
								.param("productType", "MOBILE_DATA"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.recentPrice").exists())
						.andExpect(jsonPath("$.data.averagePrice").exists())
						.andDo(document("product/market-price",
								queryParameters(
										parameterWithName("productType").description(
												"상품 타입 (필수, (MOBILE_DATA: 데이터, WIFI: 와이파이))")
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										fieldWithPath("data.recentPrice").description("최근 거래가"),
										fieldWithPath("data.averagePrice").description("평균 거래가")
								)
						));

				FindMarketPriceResponse response = productService.findMarketPrice("MOBILE_DATA");

				int expectedRecentRate = 150; // 150
				int expectedAverageRate = 225; // (150 + 300) / 2

				assertThat(response.getRecentPrice()).isEqualTo(expectedRecentRate);
				assertThat(response.getAveragePrice()).isEqualTo(expectedAverageRate);
			}
		}
	}

	@Nested
	@DisplayName("나의 판매 상품 조회 API")
	class ReadMySellingProduct {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("나의 상품의 상태가 ACTIVE 인 판매 상품을 조회한다")
			public void readMySellingProductTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());

				List<MobileData> mobileDataList = mobileDataRepository.saveAll(
						MobileDataFixture.createMobileDataList());

				List<Product> productActivList = productRepository.saveAll(
						ProductFixture.createProductList(seller, mobileDataList,
								ProductState.ACTIVE));
				productRepository.saveAll(ProductFixture.createProductList(seller, mobileDataList,
						ProductState.SOLD_OUT));

				CustomUserDetails userDetails = CustomUserDetails.from(seller);

				//when & then
				mockMvc.perform(get("/api/selling-products", seller.getId())
								.param("productState", ProductState.ACTIVE.name())
								.param("size", String.valueOf(DEFAULT_SIZE_2))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").exists())
						.andExpect(jsonPath("$.data.data.length()").value(
								Math.min(DEFAULT_SIZE_2, productActivList.size())))
						.andExpect(jsonPath("$.data.pageInfo").exists())
						.andExpect(jsonPath("$.data.pageInfo.hasNext").value(true))
						.andExpect(jsonPath("$.data.pageInfo.size").value(DEFAULT_SIZE_2))
						.andExpect(jsonPath("$.data.pageInfo.nextCursorId").exists())
						.andDo(document("product/read-my-selling-product",
								queryParameters(
										parameterWithName("productState").description(
												"조회할 판매 상품의 상태 (필수) (ACTIVE / SOLD_OUT)"),
										parameterWithName("cursorId").description("커서 아이디 (선택)")
												.optional(),
										parameterWithName("size").description(
												"페이지 크기 (선택, 기본값 = 2, 최대 = 100)").optional()
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										fieldWithPath("data").description("페이징 처리된 리뷰 데이터"),
										fieldWithPath("data.data[]").description("조회된 판매 상품 목록"),
										fieldWithPath("data.data[].productId").description(
												"상품 아이디"),
										fieldWithPath("data.data[].type").description("상품 타입"),
										fieldWithPath("data.data[].state").description("상품 상태"),
										fieldWithPath("data.data[].dataAmount").description(
														"모바일 데이터 전체량").type(JsonFieldType.NUMBER)
												.optional(),
										fieldWithPath("data.data[].remainAmount").description(
												"모바일 데이터 잔량").type(JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.data[].startTime").description(
														"와이파이 판매 시작 시간").type(JsonFieldType.NUMBER)
												.optional(),
										fieldWithPath("data.data[].endTime").description(
												"와이파이 판매 종료 시간").type(
												JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.data[].createdAt").description(
												"상품 등록 시간"),
										fieldWithPath("data.data[].updatedAt").description(
												"상품 수정 시간"),
										fieldWithPath("data.pageInfo").description("페이지 정보"),
										fieldWithPath("data.pageInfo.size").description(
												"현재 페이지 크기"),
										fieldWithPath("data.pageInfo.hasNext").description(
												"다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.nextCursorId").description(
												"다음 페이지 조회 시 사용할 커서 아이디 (다음 페이지가 없으면 null)"),
										fieldWithPath("data.count").description("조회된 전체 갯수")
								)
						));
			}
		}
	}
}
