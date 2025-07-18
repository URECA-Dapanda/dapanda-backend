package com.dapanda.product.controller;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.dto.request.MobileDataCursorRequest;
import com.dapanda.product.dto.request.UpdateMobileDataRequest;
import com.dapanda.product.dto.request.UpdateWifiRequest;
import com.dapanda.product.dto.request.WifiCursorRequest;
import com.dapanda.product.dto.response.MobileDataInfoResponse;
import com.dapanda.product.dto.response.WifiInfoResponse;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.MobileDataRepository;
import com.dapanda.product.repository.ProductImageRepository;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.product.repository.WifiRepository;
import com.dapanda.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.dapanda.TestConstants.Product.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
	private MockMvc mockMvc;
	@Autowired
	private ProductService productService;

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

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
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
				Float dataAmount = 2.0F;

				Member member = memberRepository.save(MemberFixture.createMember1());

				MobileData mobileData1 = MobileDataFixture.createMobileData(2.0F, 2.0F, 500);
				MobileData mobileData2 = MobileDataFixture.createMobileData(2.0F, 2.0F, 600);
				MobileData mobileData3 = MobileDataFixture.createMobileData(3.0F, 3.0F, 700);
				mobileDataRepository.saveAll(List.of(mobileData1, mobileData2, mobileData3));

				Product product1 = ProductFixture.createMobileDataProduct(3000,
						mobileData1.getId(), member);
				Product product2 = ProductFixture.createMobileDataProduct(4000,
						mobileData2.getId(), member);
				Product product3 = ProductFixture.createMobileDataProduct(5000,
						mobileData3.getId(), member);
				productRepository.saveAll(List.of(product1, product2, product3));
				MobileDataCursorRequest request = new MobileDataCursorRequest(null, size,
						productSortOption, dataAmount);

				// when & then
				mockMvc.perform(
								MockMvcRequestBuilders.post(
												"/api/products/mobile-data")
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data").exists())
						.andDo(document("product/get-products-mobile-data",
								requestFields(
										fieldWithPath("cursorId").description("마지막 커서 아이디 (필수 X)"),
										fieldWithPath("size").description("페이지 사이즈 (필수, 1 이상 정수)"),
										fieldWithPath("productSortOption").description(
												"정렬 조건 (필수 X, 기본값: 최신순) - RECENT(최신순), PRICE_ASC(가격 낮은순), AMOUNT_ASC(데이터 용량 적은순), AMOUNT_DESC(데이터 용량 많은순"),
										fieldWithPath("dataAmount").description("데이터 양 (필수 X)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.data").description("상품 데이터 배열"),
										fieldWithPath("data.data[].id").description(
												"상품 아이디"),
										fieldWithPath("data.data[].price").description(
												"상품 가격"),
										fieldWithPath("data.data[].itemId").description(
												"모바일 데이터 아이디"),
										fieldWithPath("data.data[].memberName").description(
												"등록한 회원 이름"),
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

					// given
					List<MobileDataCursorRequest> invalidRequests = List.of(
							new MobileDataCursorRequest(3L, 0, "RECENT", 2.0F),
							new MobileDataCursorRequest(3L, null, "RECENT", 2.0F)
					);

					// when & then
					for (MobileDataCursorRequest request : invalidRequests) {
						mockMvc.perform(MockMvcRequestBuilders.post(
												"/api/products/mobile-data")
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request))
								)
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.message").value(
										containsString("유효하지 않은 파라미터입니다.")))
								.andExpect(jsonPath("$.code").value(1006))
								.andDo(document(
										"product/get-products-mobile-data-size-validation-error"));
					}
				}

				@Test
				@DisplayName("데이터 상품 목록 조회 시 상품 정렬 조건이 유효하지 않으면 예외를 던진다")
				void throwExceptionWhenProductSortOptionIsInvalid() throws Exception {

					// given
					MobileDataCursorRequest request = new MobileDataCursorRequest(3L, 2,
							"RECENT123", 2.0F);

					// when & then
					mockMvc.perform(MockMvcRequestBuilders.post(
											"/api/products/mobile-data")
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(request))
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

				Wifi wifi1 = WifiFixture.createWifi("제목1", "내용1", 30.0, 126.0,
						LocalDateTime.of(2025, 7, 14, 10, 0),
						LocalDateTime.of(2025, 7, 14, 18, 0));
				Wifi wifi2 = WifiFixture.createWifi("제목2", "내용2", 30.0, 126.0,
						LocalDateTime.of(2025, 7, 14, 10, 0),
						LocalDateTime.of(2025, 7, 14, 18, 0));
				Wifi wifi3 = WifiFixture.createWifi("제목3", "내용3", 30.0, 126.0,
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

				WifiCursorRequest request = new WifiCursorRequest(cursorId, size,
						productSortOption, isOpen, latitude, longitude);

				// when & then
				mockMvc.perform(MockMvcRequestBuilders.post(
										"/api/products/wifi")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data").exists())
						.andDo(print())
						.andDo(document("product/get-products-wifi",
								requestFields(
										fieldWithPath("cursorId").description("마지막 커서 아이디 (필수 X)"),
										fieldWithPath("size").description("페이지 사이즈 (필수, 1 이상 정수)"),
										fieldWithPath("productSortOption").description(
												"정렬 조건 (필수 X) - PRICE_ASC(가격 낮은순), AVERAGE_RATE_DESC(평점 높은순)"),
										fieldWithPath("open").description("영업중 여부(필수 X)"),
										fieldWithPath("latitude").description("사용자의 위도"),
										fieldWithPath("longitude").description("사용자의 경도")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.data").description("상품 데이터 배열"),
										fieldWithPath("data.data[].id").description(
												"상품 아이디"),
										fieldWithPath("data.data[].price").description(
												"상품 가격"),
										fieldWithPath("data.data[].itemId").description(
												"와이파이 아이디"),
										fieldWithPath("data.data[].memberName").description(
												"등록한 회원 이름"),
										fieldWithPath("data.data[].title").description("게시물 제목"),
										fieldWithPath("data.data[].imageUrl").description(
												"대표 이미지 URL").optional(),
										fieldWithPath("data.data[].latitude").description("위도"),
										fieldWithPath("data.data[].longitude").description("경도"),
										fieldWithPath("data.data[].averageRate").description(
												"평균 평점"),
										fieldWithPath("data.data[].distanceKm").description(
												"현 위치로부터 거리 (km)"),
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
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("와이파이 상품 목록 조회 시 size가 null이거나 1 미만이면 예외를 던진다")
			void throwExceptionWhenSizeIsNullOrLessThan1() throws Exception {

				// given
				List<WifiCursorRequest> invalidRequests = List.of(
						new WifiCursorRequest(3L, 0, "RECENT", true, 30.0, 126.0),
						new WifiCursorRequest(4L, null, "RECENT", true, 30.0, 126.0)
				);

				// when & then
				for (WifiCursorRequest request : invalidRequests) {
					mockMvc.perform(MockMvcRequestBuilders.post(
											"/api/products/wifi")
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(request))
							)
							.andExpect(status().isBadRequest())
							.andExpect(jsonPath("$.message").value(
									containsString("유효하지 않은 파라미터입니다.")))
							.andExpect(jsonPath("$.code").value(1006))
							.andDo(document(
									"product/get-products-wifi-size-validation-error"));
				}
			}

			@Test
			@DisplayName("와아파이 상품 목록 조회 시 위도, 경도 값이 유효하지 않으면 예외를 던진다")
			void throwExceptionWhenProductSortOptionIsInvalid() throws Exception {

				// given
				WifiCursorRequest request = new WifiCursorRequest(3L, 2,
						"RECENT123", true, null, null);

				// when & then
				mockMvc.perform(MockMvcRequestBuilders.post(
										"/api/products/wifi")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.message").value(
								containsString("유효하지 않은 파라미터입니다.")))
						.andExpect(jsonPath("$.code").value(1006))
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
						MobileDataFixture.createMobileData(DATA_AMOUNT, REMAIN_AMOUNT,
								PRICE_PER_100MB));

				productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE, mobileData.getId(), member));

				// when & then
				mockMvc.perform(get("/api/products/mobile-data/{productId}", PRODUCT_ID)
								.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.data.productId").value(PRODUCT_ID))
						.andExpect(jsonPath("$.data.itemId").value(mobileData.getId()))
						.andExpect(jsonPath("$.data.price").value(PRICE))
						.andExpect(jsonPath("$.data.memberId").value(member.getId()))
						.andExpect(jsonPath("$.data.memberName").value(member.getName()))
						.andExpect(jsonPath("$.data.remainAmount").value(REMAIN_AMOUNT))
						.andExpect(jsonPath("$.data.pricePer100MB").value(PRICE_PER_100MB))
						.andExpect(jsonPath("$.data.averageRate").exists())
						.andExpect(jsonPath("$.data.reviewCount").exists())
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
										fieldWithPath("data.remainAmount").description("남은 데이터양"),
										fieldWithPath("data.pricePer100MB").description(
												"100MB 당 가격"),
										fieldWithPath("data.averageRate").description("평균 별점"),
										fieldWithPath("data.reviewCount").description("리뷰 수"),
										fieldWithPath("data.updatedAt").description("수정된 시간")
								))
						);

				MobileDataInfoResponse actualResponse = productService.findMobileDataInfo(
						PRODUCT_ID);

				assertThat(actualResponse.getProductId()).isEqualTo(PRODUCT_ID);
				assertThat(actualResponse.getItemId()).isEqualTo(mobileData.getId());
				assertThat(actualResponse.getRemainAmount()).isEqualTo(REMAIN_AMOUNT);
				assertThat(actualResponse.getPricePer100MB()).isEqualTo(PRICE_PER_100MB);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("데이터 상품 상세 조회 시 상품 아이디가 존재하지 않으면 예외를 던진다")
			void throwsExceptionWhenProductIdNotExist() throws Exception {

				// given & when & then
				mockMvc.perform(get("/api/products/mobile-data/{productId}", INVALID_PRODUCT_ID)
								.contentType(MediaType.APPLICATION_JSON))
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
						MobileDataFixture.createMobileData(DATA_AMOUNT, REMAIN_AMOUNT,
								PRICE_PER_100MB));

				productRepository.save(
						ProductFixture.createMobileDataProductInactive(PRICE, mobileData.getId(),
								member));

				// when & then
				mockMvc.perform(get("/api/products/mobile-data/{productId}", PRODUCT_ID)
								.contentType(MediaType.APPLICATION_JSON))
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
	@DisplayName("와아파이 상품 상세 조회 API")
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
						WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE,
								START_TIME, END_TIME));

				productRepository.save(
						ProductFixture.createWifiProduct(PRICE, wifi.getId(), member));

				productImageRepository.save(
						ProductImageFixture.createProductImage(IMAGE_URL_1, 3, wifi.getId()));
				productImageRepository.save(
						ProductImageFixture.createProductImage(IMAGE_URL_2, 2, wifi.getId()));

				// when & then
				mockMvc.perform(get("/api/products/wifi/{productId}", PRODUCT_ID)
								.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.data.productId").value(PRODUCT_ID))
						.andExpect(jsonPath("$.data.itemId").value(wifi.getId()))
						.andExpect(jsonPath("$.data.price").value(PRICE))
						.andExpect(jsonPath("$.data.memberId").value(member.getId()))
						.andExpect(jsonPath("$.data.memberName").value(member.getName()))
						.andExpect(jsonPath("$.data.title").value(TITLE))
						.andExpect(jsonPath("$.data.content").value(CONTENT))
						.andExpect(jsonPath("$.data.latitude").value(LATITUDE))
						.andExpect(jsonPath("$.data.longitude").value(LONGITUDE))
						.andExpect(jsonPath("$.data.content").value(CONTENT))
						.andExpect(jsonPath("$.data.averageRate").exists())
						.andExpect(jsonPath("$.data.reviewCount").exists())
						.andExpect(jsonPath("$.data.imageUrls").exists())
						.andExpect(jsonPath("$.data.startTime").exists())
						.andExpect(jsonPath("$.data.endTime").exists())
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
										fieldWithPath("data.title").description("게시물 제목"),
										fieldWithPath("data.content").description("게시물 내용"),
										fieldWithPath("data.latitude").description("위도"),
										fieldWithPath("data.longitude").description("경도"),
										fieldWithPath("data.averageRate").description("평균 별점"),
										fieldWithPath("data.reviewCount").description("리뷰 수"),
										fieldWithPath("data.imageUrls[]").description(
												"이미지 URL (우선순위 높은순)"),
										fieldWithPath("data.startTime").description("시작 시간"),
										fieldWithPath("data.endTime").description("종료 시간"),
										fieldWithPath("data.updatedAt").description("수정된 시간")
								))
						);

				WifiInfoResponse actualResponse = productService.findWifiInfo(PRODUCT_ID);

				assertThat(actualResponse.getProductId()).isEqualTo(PRODUCT_ID);
				assertThat(actualResponse.getItemId()).isEqualTo(wifi.getId());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("와이파이 상품 상세 조회 시 상품 아이디가 존재하지 않으면 예외를 던진다")
			void throwsExceptionWhenProductIdNotExist() throws Exception {

				// given & when & then
				mockMvc.perform(get("/api/products/wifi/{productId}", INVALID_PRODUCT_ID)
								.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest())
						.andDo(document("product/get-wifi-info-not-exist-product-id-error",
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

				Wifi wifi = wifiRepository.save(
						WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE,
								START_TIME, END_TIME));

				productRepository.save(
						ProductFixture.createWifiProductInactive(PRICE, wifi.getId(), member));

				// when & then
				mockMvc.perform(get("/api/products/wifi/{productId}", PRODUCT_ID)
								.contentType(MediaType.APPLICATION_JSON))
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
				Member member = memberRepository.save(MemberFixture.createMember1());
				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT, BEFORE_REMAIN_AMOUNT,
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE, mobileData.getId(), member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				UpdateMobileDataRequest request = new UpdateMobileDataRequest(product.getId(),
						NEW_PRICE, CHANGED_AMOUNT, SPLIT_TYPE);

				// when & then
				mockMvc.perform(put("/api/products/mobile-data")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.data.productId").value(product.getId()))
						.andDo(document("product/put-mobile-data",
								requestFields(
										fieldWithPath("productId").description("상품 아이디 (필수)"),
										fieldWithPath("price").description("상품 가격 (필수)"),
										fieldWithPath("changedAmount").description(
												"데이터 변화량 (필수, 음수/양수)"),
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
				assertThat(updatedProduct.getPrice()).isEqualTo(NEW_PRICE);
				assertThat(updatedMobileData.getDataAmount()).isEqualTo(
						BEFORE_DATA_AMOUNT + CHANGED_AMOUNT);
				assertThat(updatedMobileData.getRemainAmount()).isEqualTo(
						BEFORE_REMAIN_AMOUNT + CHANGED_AMOUNT);
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
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE, mobileData.getId(), member1));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member2.getId());

				UpdateMobileDataRequest request = new UpdateMobileDataRequest(product.getId(),
						NEW_PRICE, CHANGED_AMOUNT, SPLIT_TYPE);

				// when & then
				mockMvc.perform(put("/api/products/mobile-data")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
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
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE, mobileData.getId(), member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				UpdateMobileDataRequest request = new UpdateMobileDataRequest(product.getId(),
						NEW_PRICE, CHANGED_AMOUNT, SPLIT_TYPE);

				// when & then
				mockMvc.perform(put("/api/products/mobile-data")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
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
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE, mobileData.getId(), member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				UpdateMobileDataRequest request = new UpdateMobileDataRequest(product.getId(),
						NEW_PRICE, EXCEED_CHANGED_AMOUNT, SPLIT_TYPE);

				// when & then
				mockMvc.perform(put("/api/products/mobile-data")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
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
						WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, START_TIME,
								END_TIME));
				Product product = productRepository.save(
						ProductFixture.createWifiProduct(PRICE, wifi.getId(), member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				UpdateWifiRequest request = new UpdateWifiRequest(product.getId(), NEW_PRICE,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						START_TIME, END_TIME);

				// when & then
				mockMvc.perform(put("/api/products/wifi")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
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
				assertThat(updatedProduct.getPrice()).isEqualTo(NEW_PRICE);
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
						WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, START_TIME,
								END_TIME));
				Product product = productRepository.save(
						ProductFixture.createWifiProduct(PRICE, wifi.getId(), member1));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member2.getId());

				UpdateWifiRequest request = new UpdateWifiRequest(product.getId(), NEW_PRICE,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						START_TIME, END_TIME);

				// when & then
				mockMvc.perform(put("/api/products/wifi")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
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
						WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, START_TIME,
								END_TIME));
				Product product = productRepository.save(
						ProductFixture.createWifiProduct(PRICE, wifi.getId(), member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				UpdateWifiRequest request = new UpdateWifiRequest(product.getId(), NEW_PRICE,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						WRONG_START_TIME, WRONG_END_TIME);

				// when & then
				mockMvc.perform(put("/api/products/wifi")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
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
				MobileData mobileData = mobileDataRepository.save(
						MobileDataFixture.createMobileData(DATA_AMOUNT, REMAIN_AMOUNT,
								PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE, mobileData.getId(), member));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);
				given(userDetails.getId()).willReturn(member.getId());

				// when & then
				mockMvc.perform(delete("/api/products/{productId}", PRODUCT_ID)
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
						MobileDataFixture.createMobileData(DATA_AMOUNT,
								REMAIN_AMOUNT, PRICE_PER_100MB));
				Product product = productRepository.save(
						ProductFixture.createMobileDataProduct(PRICE, mobileData.getId(), member));

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
						MobileDataFixture.createMobileData(DATA_AMOUNT,
								REMAIN_AMOUNT, PRICE_PER_100MB));
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
}
