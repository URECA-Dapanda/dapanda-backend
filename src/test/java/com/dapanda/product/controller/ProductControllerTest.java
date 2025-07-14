package com.dapanda.product.controller;

import com.dapanda.TestConfig;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.dto.request.MobileDataCursorRequest;
import com.dapanda.product.dto.request.WifiCursorRequest;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.MobileDataRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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

				Wifi wifi1 = WifiFixture.createWifi1("제목1", "내용1", 30.0, 126.0,
						LocalDateTime.of(2025, 7, 14, 10, 0),
						LocalDateTime.of(2025, 7, 14, 18, 0));
				Wifi wifi2 = WifiFixture.createWifi1("제목2", "내용2", 30.0, 126.0,
						LocalDateTime.of(2025, 7, 14, 10, 0),
						LocalDateTime.of(2025, 7, 14, 18, 0));
				Wifi wifi3 = WifiFixture.createWifi1("제목3", "내용3", 30.0, 126.0,
						LocalDateTime.of(2025, 7, 14, 10, 0),
						LocalDateTime.of(2025, 7, 14, 18, 0));
				wifiRepository.saveAll(List.of(wifi1, wifi2, wifi3));

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
										fieldWithPath("data.data[].latitude").description("위도"),
										fieldWithPath("data.data[].longitude").description("경도"),
										fieldWithPath("data.data[].imageUrl").description(
												"이미지 URL"),
										fieldWithPath("data.data[].averageRate").description(
												"평균 평점"),
										fieldWithPath("data.data[].distanceKm").description(
												"현 위치로부터 거리 (km)"),
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
}
