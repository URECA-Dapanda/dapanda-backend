package com.dapanda.review.controller;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.product.repository.WifiRepository;
import com.dapanda.review.dto.request.CreateReviewRequest;
import com.dapanda.review.dto.request.UpdateReviewRequest;
import com.dapanda.review.entity.Review;
import com.dapanda.review.entity.ReviewFixture;
import com.dapanda.review.repository.ReviewRepository;
import com.dapanda.trade.entity.*;
import com.dapanda.trade.repository.TradeDetailsRepository;
import com.dapanda.trade.repository.TradeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static com.dapanda.TestConstants.Member.USER_DETAILS_MEMBER_ID;
import static com.dapanda.TestConstants.Pagination.DEFAULT_REVIEW_SORT_OPTION;
import static com.dapanda.TestConstants.Pagination.DEFAULT_SIZE_2;
import static com.dapanda.TestConstants.Review.*;
import static com.dapanda.TestConstants.Trade.TRADE_ID_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("리뷰 컨트롤러 테스트")
class ReviewControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TradeRepository tradeRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private TradeDetailsRepository tradeDetailsRepository;

	@Autowired
	private WifiRepository wifiRepository;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);

		cleanupDatabase();
	}

	private void cleanupDatabase() {

		entityManager.clear();

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

		jdbcTemplate.execute("TRUNCATE TABLE review");
		jdbcTemplate.execute("TRUNCATE TABLE trade_details");
		jdbcTemplate.execute("TRUNCATE TABLE trade");
		jdbcTemplate.execute("TRUNCATE TABLE product");
		jdbcTemplate.execute("TRUNCATE TABLE wifi");
		jdbcTemplate.execute("TRUNCATE TABLE member");

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
	}

	@Nested
	@DisplayName("리뷰 등록 API")
	class SaveReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("등록된 리뷰 아이디를 반환한다")
			public void createReviewTest() throws Exception {

				//given
				Member buyer = memberRepository.save(MemberFixture.createMember2());
				Trade trade = tradeRepository.save(
						TradeFixture.createTradeWifi(buyer));

				CreateReviewRequest request = new CreateReviewRequest(RATING, COMMENT);

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				//when & then
				mockMvc.perform(post("/api/trades/{tradeId}/reviews", trade.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.reviewId").exists())
						.andDo(document("review/create-review",
								pathParameters(
										parameterWithName("tradeId").description(
												"리뷰할 판매자의 상품의 거래 아이디 (필수)")
								),
								requestFields(
										fieldWithPath("rating").description(
												"리뷰 평점 (필수, 1.0 ~ 5.0)"),
										fieldWithPath("comment").description("리뷰 코멘트 (필수, 최대 50자)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.reviewId").description("생성된 리뷰 아이디")
								))
						);

				Optional<Review> saveReview = reviewRepository.findAll().stream().findFirst();

				assertThat(saveReview).isPresent();
				assertThat(saveReview.get().getRating()).isEqualTo(request.rating());
				assertThat(saveReview.get().getComment()).isEqualTo(request.comment());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("필수 필드가 누락되면 BadRequest 를 반환한다")
			public void validateRequiredFields() throws Exception {

				// given
				CreateReviewRequest request = new CreateReviewRequest(null, null);

				// when & then
				mockMvc.perform(post("/api/trades/{tradeId}/reviews", TRADE_ID_1)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("review/create-review/validation-error"));
			}
		}
	}

	@Nested
	@DisplayName("회원이 받은 리뷰 조회 API")
	class ReadSellerReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("기본 페이징 조건으로 회원이 받은 리뷰를 조회한다")
			public void readReceivedReviewTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());

				List<Member> members = memberRepository.saveAll(List.of(
						MemberFixture.createMember2(),
						MemberFixture.createMember3(),
						MemberFixture.createMember4(),
						MemberFixture.createMember5()
				));

				Wifi wifi = wifiRepository.save(WifiFixture.createWifi());

				Product product = productRepository.save(ProductFixture.createWifiProduct(seller, wifi));

				List<Trade> tradeFixtures = new ArrayList<>();

				for (Member member : members) {
					tradeFixtures.add(TradeFixture.createTradeWifi(member));
				}

				List<Trade> trades = tradeRepository.saveAll(tradeFixtures);

				List<TradeDetails> tradeDetailsFixtures = new ArrayList<>();

				for (Trade trade : trades) {
					tradeDetailsFixtures.add(
							TradeDetailsFixture.createTradeDetails(product, trade));
				}
				tradeDetailsRepository.saveAll(tradeDetailsFixtures);

				List<Review> reviewFixtures = new ArrayList<>();

				for (Trade trade : trades) {
					reviewFixtures.add(ReviewFixture.createReview1(trade));
				}

				List<Review> reviews = reviewRepository.saveAll(reviewFixtures);

				//when & then
				mockMvc.perform(get("/api/members/{memberId}/reviews/received", seller.getId())
								.param("size", String.valueOf(DEFAULT_SIZE_2))
								.param("reviewSortOption", DEFAULT_REVIEW_SORT_OPTION))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").exists())
						.andExpect(jsonPath("$.data.data.length()").value(
								Math.min(DEFAULT_SIZE_2, reviews.size())))
						.andExpect(jsonPath("$.data.pageInfo").exists())
						.andExpect(jsonPath("$.data.pageInfo.hasNext").isBoolean())
						.andExpect(jsonPath("$.data.pageInfo.size").isNumber())
						.andExpect(jsonPath("$.data.pageInfo.nextCursorId").isNumber())
						.andDo(document("review/read-received-review",
								pathParameters(
										parameterWithName("memberId").description(
												"받은 리뷰를 조회할 회원의 아이디 (필수)")
								),
								queryParameters(
										parameterWithName("cursorId").description("커서 아이디 (선택)")
												.optional(),
										parameterWithName("size").description(
												"페이지 크기 (선택, 기본값 = 2, 최대 = 100)").optional(),
										parameterWithName("reviewSortOption").description(
														"리뷰 정렬 옵션 (선택, 기본값 = RECENT: 최신순, OLDEST: 오래된순)")
												.optional()
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										fieldWithPath("data").description("페이징 처리된 리뷰 데이터"),
										// data.data[] 배열 내의 각 리뷰 객체 필드
										fieldWithPath("data.data[]").description("조회된 리뷰 목록"),
										fieldWithPath("data.data[].reviewId").description("리뷰 아이디"),
										fieldWithPath("data.data[].rating").description("리뷰 평점"),
										fieldWithPath("data.data[].comment").description("리뷰 코멘트"),
										fieldWithPath("data.data[].createdAt").description(
												"리뷰 생성 시간"),
										fieldWithPath("data.data[].updatedAt").description(
												"리뷰 최종 수정 시간"),
										// Member 정보
										fieldWithPath("data.data[].reviewerId").description(
												"리뷰 작성자의 아이디"),
										fieldWithPath("data.data[].reviewerName").description(
												"리뷰 작성자의 이름"),
										// Trade 정보
										fieldWithPath("data.data[].tradeId").description(
												"해당 리뷰가 연결된 거래 아이디"),
										fieldWithPath("data.data[].dataAmount").description(
														"거래된 데이터 양 (데이터 상품인 경우에만 존재)")
												.type(JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.data[].timeAmount").description(
														"거래된 시간 양 (시간 상품인 경우에만 존재)")
												.type(JsonFieldType.NUMBER).optional(),
										// Product 정보
										fieldWithPath("data.data[].productId").description(
												"리뷰가 작성된 상품 아이디"),
										fieldWithPath("data.data[].itemType").description(
												"상품 유형 (예: MOBILE_DATA, TIME 등)"),
										// Wifi 정보
										fieldWithPath("data.data[].title").description(
												"WIFI 상품의 제목"),
										// data.pageInfo 필드
										fieldWithPath("data.pageInfo").description("페이지 정보"),
										fieldWithPath("data.pageInfo.size").description(
												"현재 페이지 크기"),
										fieldWithPath("data.pageInfo.hasNext").description(
												"다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.nextCursorId").description(
												"다음 페이지 조회 시 사용할 커서 아이디 (다음 페이지가 없으면 null)")
								)
						));
			}
		}
	}

	@Nested
	@DisplayName("내가 받은 리뷰 조회 API")
	class ReadMyReceivedReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("기본 페이징 조건으로 내가 받은 리뷰를 조회한다")
			public void readReceivedReviewTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());

				List<Member> members = memberRepository.saveAll(List.of(
						MemberFixture.createMember2(),
						MemberFixture.createMember3(),
						MemberFixture.createMember4(),
						MemberFixture.createMember5()
				));

				Wifi wifi = wifiRepository.save(WifiFixture.createWifi());

				Product product = productRepository.save(ProductFixture.createWifiProduct(seller, wifi));

				List<Trade> tradeFixtures = new ArrayList<>();

				for (Member member : members) {
					tradeFixtures.add(TradeFixture.createTradeWifi(member));
				}

				List<Trade> trades = tradeRepository.saveAll(tradeFixtures);

				List<TradeDetails> tradeDetailsFixtures = new ArrayList<>();

				for (Trade trade : trades) {
					tradeDetailsFixtures.add(
							TradeDetailsFixture.createTradeDetails(product, trade));
				}

				tradeDetailsRepository.saveAll(tradeDetailsFixtures);

				List<Review> reviewFixtures = new ArrayList<>();

				for (Trade trade : trades) {
					reviewFixtures.add(ReviewFixture.createReview1(trade));
				}

				List<Review> reviews = reviewRepository.saveAll(reviewFixtures);

				CustomUserDetails userDetails = CustomUserDetails.from(seller);

				//when & then
				mockMvc.perform(get("/api/reviews/received")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").exists())
						.andExpect(jsonPath("$.data.data.length()").value(
								Math.min(DEFAULT_SIZE_2, reviews.size())))
						.andExpect(jsonPath("$.data.pageInfo").exists())
						.andExpect(jsonPath("$.data.pageInfo.hasNext").isBoolean())
						.andExpect(jsonPath("$.data.pageInfo.size").isNumber())
						.andExpect(jsonPath("$.data.pageInfo.nextCursorId").isNumber())
						.andDo(document("review/read-my-received-review",
								queryParameters(
										parameterWithName("cursorId").description("커서 아이디 (선택)")
												.optional(),
										parameterWithName("size").description(
												"페이지 크기 (선택, 기본값 = 2, 최대 = 100)").optional(),
										parameterWithName("reviewSortOption").description(
														"리뷰 정렬 옵션 (선택, 기본값 = RECENT: 최신순, OLDEST: 오래된순)")
												.optional()
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										fieldWithPath("data").description("페이징 처리된 리뷰 데이터"),
										// data.data[] 배열 내의 각 리뷰 객체 필드
										fieldWithPath("data.data[]").description("조회된 리뷰 목록"),
										fieldWithPath("data.data[].reviewId").description("리뷰 아이디"),
										fieldWithPath("data.data[].rating").description("리뷰 평점"),
										fieldWithPath("data.data[].comment").description("리뷰 코멘트"),
										fieldWithPath("data.data[].createdAt").description(
												"리뷰 생성 시간"),
										fieldWithPath("data.data[].updatedAt").description(
												"리뷰 최종 수정 시간"),
										// Member 정보
										fieldWithPath("data.data[].reviewerId").description(
												"리뷰 작성자의 아이디"),
										fieldWithPath("data.data[].reviewerName").description(
												"리뷰 작성자의 이름"),
										// Trade 정보
										fieldWithPath("data.data[].tradeId").description(
												"해당 리뷰가 연결된 거래 아이디"),
										fieldWithPath("data.data[].dataAmount").description(
														"거래된 데이터 양 (데이터 상품인 경우에만 존재)")
												.type(JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.data[].timeAmount").description(
														"거래된 시간 양 (시간 상품인 경우에만 존재)")
												.type(JsonFieldType.NUMBER).optional(),
										// Product 정보
										fieldWithPath("data.data[].productId").description(
												"리뷰가 작성된 상품 아이디"),
										fieldWithPath("data.data[].itemType").description(
												"상품 유형 (예: MOBILE_DATA, TIME 등)"),
										// Wifi 정보
										fieldWithPath("data.data[].title").description(
												"WIFI 상품의 제목"),
										// data.pageInfo 필드
										fieldWithPath("data.pageInfo").description("페이지 정보"),
										fieldWithPath("data.pageInfo.size").description(
												"현재 페이지 크기"),
										fieldWithPath("data.pageInfo.hasNext").description(
												"다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.nextCursorId").description(
												"다음 페이지 조회 시 사용할 커서 아이디 (다음 페이지가 없으면 null)")
								)
						));
			}
		}
	}

	@Nested
	@DisplayName("내가 작성한 리뷰 조회 API")
	class ReadMyWrittenReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("기본 페이징 조건으로 작성한 리뷰를 조회한다")
			public void readWrittenReviewTest() throws Exception {

				Member buyer = memberRepository.save(MemberFixture.createMember1());

				List<Member> members = memberRepository.saveAll(List.of(
						MemberFixture.createMember2(),
						MemberFixture.createMember3(),
						MemberFixture.createMember4(),
						MemberFixture.createMember5()
				));

				List<Product> productFixtures = new ArrayList<>();

				for (Member member : members) {

					productFixtures.add(ProductFixture.createProduct1(member));
				}

				List<Product> products = productRepository.saveAll(productFixtures);

				List<Trade> tradesFixture = new ArrayList<>();

				for (int i = 0; i < products.size(); i++) {

					tradesFixture.add(TradeFixture.createTradeWifi(buyer));
				}

				List<Trade> trades = tradeRepository.saveAll(tradesFixture);

				List<TradeDetails> tradeDetailsFixtures = new ArrayList<>();

				for (int i = 0; i < trades.size(); i++) {
					tradeDetailsFixtures.add(
							TradeDetailsFixture.createTradeDetails(products.get(i), trades.get(i)));
				}
				tradeDetailsRepository.saveAll(tradeDetailsFixtures);

				List<Review> reviewFixtures = new ArrayList<>();

				for (Trade trade : trades) {
					reviewFixtures.add(ReviewFixture.createReview1(trade));
				}

				List<Review> reviews = reviewRepository.saveAll(reviewFixtures);

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				//when & then
				mockMvc.perform(get("/api/reviews/wrote")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").exists())
						.andExpect(jsonPath("$.data.data.length()").value(
								Math.min(DEFAULT_SIZE_2, reviews.size())))
						.andExpect(jsonPath("$.data.pageInfo").exists())
						.andExpect(jsonPath("$.data.pageInfo.hasNext").isBoolean())
						.andExpect(jsonPath("$.data.pageInfo.size").isNumber())
						.andExpect(jsonPath("$.data.pageInfo.nextCursorId").isNumber())
						.andDo(document("review/read-my-wrote-review",
								queryParameters(
										parameterWithName("cursorId").description("커서 아이디 (선택)")
												.optional(),
										parameterWithName("size").description(
												"페이지 크기 (선택, 기본값 = 2, 최대 = 100)").optional(),
										parameterWithName("reviewSortOption").description(
														"리뷰 정렬 옵션 (선택, 기본값 = RECENT: 최신순, OLDEST: 오래된순)")
												.optional()
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										fieldWithPath("data").description("페이징 처리된 리뷰 데이터"),
										// data.data[] 배열 내의 각 리뷰 객체 필드
										fieldWithPath("data.data[]").description("조회된 리뷰 목록"),
										fieldWithPath("data.data[].reviewId").description("리뷰 아이디"),
										fieldWithPath("data.data[].rating").description("리뷰 평점"),
										fieldWithPath("data.data[].comment").description("리뷰 코멘트"),
										fieldWithPath("data.data[].createdAt").description(
												"리뷰 생성 시간"),
										fieldWithPath("data.data[].updatedAt").description(
												"리뷰 최종 수정 시간"),
										// Member 정보
										fieldWithPath("data.data[].revieweeId").description(
												"리뷰 받은 회원 아이디"),
										fieldWithPath("data.data[].revieweeName").description(
												"리뷰 받은 회원 이름"),
										// Trade 정보
										fieldWithPath("data.data[].tradeId").description(
												"해당 리뷰가 연결된 거래 아이디"),
										fieldWithPath("data.data[].dataAmount").description(
														"거래된 데이터 양 (데이터 상품인 경우에만 존재)")
												.type(JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.data[].timeAmount").description(
														"거래된 시간 양 (시간 상품인 경우에만 존재)")
												.type(JsonFieldType.NUMBER).optional(),
										// Product 정보
										fieldWithPath("data.data[].productId").description(
												"리뷰가 작성된 상품 아이디"),
										fieldWithPath("data.data[].itemType").description(
												"상품 유형 (예: MOBILE_DATA, TIME 등)"),
										// data.pageInfo 필드
										fieldWithPath("data.pageInfo").description("페이지 정보"),
										fieldWithPath("data.pageInfo.size").description(
												"현재 페이지 크기"),
										fieldWithPath("data.pageInfo.hasNext").description(
												"다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.nextCursorId").description(
												"다음 페이지 조회 시 사용할 커서 아이디 (다음 페이지가 없으면 null)")
								)
						));
			}
		}
	}

	@Nested
	@DisplayName("리뷰 단건 조회 API")
	class ReadReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("리뷰 단건을 조회한다")
			public void readReviewTest() throws Exception {

				//given
				Member buyer = memberRepository.save(MemberFixture.createMember2());
				Trade trade = tradeRepository.save(TradeFixture.createTradeWifi(buyer));
				Review review = reviewRepository.save(ReviewFixture.createReview1(trade));

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				//when & then
				mockMvc.perform(get("/api/reviews/{reviewId}", review.getId())
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.reviewId").value(review.getId()))
						.andExpect(jsonPath("$.data.rating").value(review.getRating()))
						.andExpect(jsonPath("$.data.comment").value(review.getComment()))
						.andDo(document("review/read-review",
								pathParameters(
										parameterWithName("reviewId").description(
												"단건 조회할 리뷰 아이디 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										fieldWithPath("data.reviewId").description("리뷰 아이디"),
										fieldWithPath("data.rating").description("리뷰 평점"),
										fieldWithPath("data.comment").description("리뷰 코멘트")
								)
						));
			}
		}
	}

	@Nested
	@DisplayName("리뷰 삭제 API")
	class DeleteReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("삭제가 성공하면 200응답을 반환한다")
			public void deleteReviewTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());
				Trade trade = tradeRepository.save(TradeFixture.createTradeWifi(buyer));
				Review review = reviewRepository.save(ReviewFixture.createReview1(trade));

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				//when & then
				mockMvc.perform(delete("/api/reviews/{reviewId}", review.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andDo(document("review/delete-review",
								pathParameters(
										parameterWithName("reviewId").description("삭제할 리뷰 아이디 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								))
						);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("필수 필드가 누락되면 BadRequest 를 반환한다")
			public void validateRequiredFields() throws Exception {

				// given
				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(USER_DETAILS_MEMBER_ID);

				// when & then
				mockMvc.perform(delete("/api/reviews/{reviewId}", REVIEW_ID)
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("review/delete-review/validation-error"));
			}
		}
	}

	@Nested
	@DisplayName("리뷰 수정 API")
	class UpdateReview {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("리뷰 수정이 완료되면 리뷰 아이디를 반환한다")
			public void updateReviewTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());
				Trade trade = tradeRepository.save(TradeFixture.createTradeWifi(buyer));
				Review review = reviewRepository.save(ReviewFixture.createReview1(trade));

				UpdateReviewRequest request = new UpdateReviewRequest(NEW_RATING, NEW_COMMENT);

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				//when & then
				mockMvc.perform(patch("/api/reviews/{reviewId}", review.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.reviewId").value(review.getId()))
						.andDo(document("review/update-review",
								pathParameters(
										parameterWithName("reviewId").description("수정할 리뷰 아이디 (필수)")
								),
								requestFields(
										fieldWithPath("rating").description("수정할 평점 (필수)"),
										fieldWithPath("comment").description("수정할 코멘트 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data.reviewId").description("수정된 리뷰 아이디")
								))
						);

				Review updatedReview = reviewRepository.findById(review.getId()).orElseThrow();
				assertThat(updatedReview.getId()).isEqualTo(review.getId());
				assertThat(updatedReview.getRating()).isEqualTo(request.rating());
				assertThat(updatedReview.getComment()).isEqualTo(request.comment());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("필수 필드가 누락되면 BadRequest 를 반환한다")
			public void validateRequiredFields() throws Exception {

				//given
				UpdateReviewRequest request = new UpdateReviewRequest(null, null);

				//when & then
				mockMvc.perform(patch("/api/reviews/{reviewId}", REVIEW_ID)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isBadRequest())
						.andDo(document("review/update-review/validation-error"));
			}
		}
	}
}
