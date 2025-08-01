package com.dapanda.member.controller;

import static com.dapanda.TestConstants.Member.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.dto.request.UpdateProfileImageRequest;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.MobileDataRepository;
import com.dapanda.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("회원 컨트롤러 테스트")
class MemberControllerTest {

	@Autowired
	private WebApplicationContext context;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private MobileDataRepository mobileDataRepository;
	@Autowired
	private ProductRepository productRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);

		cleanupDatabase();
	}

	private void cleanupDatabase() {

		entityManager.clear();

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

		jdbcTemplate.execute("TRUNCATE TABLE member");

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
	}

	@Nested
	@DisplayName("캐시 조회 API")
	class FindCash {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 통합 상품 일반 구매를 성공한다")
			void findCash() throws Exception {

				// given
				Member member = MemberFixture.createMember1();
				ReflectionTestUtils.setField(member, "cash", CASH_5000);
				memberRepository.save(member);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/members/cash")
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.cash").value(CASH_5000))
						.andDo(document("member/get-cash",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.cash").description("회원이 보유한 캐시")
								))
						);
			}
		}
	}

	@Nested
	@DisplayName("구매/판매 데이터양 조회 API")
	class findPurchaseSaleData {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("회원의 구매 데이터양 조회를 성공한다")
			void findBuyingCash() throws Exception {

				// given
				Member member = MemberFixture.createMember1();
				ReflectionTestUtils.setField(member, "buyingData", BUYING_DATA);
				memberRepository.save(member);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/members/buying-data")
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").value(BUYING_DATA))
						.andDo(document("member/get-buying-data",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.data").description("회원이 구매한 데이터양")
								))
						);
			}

			@Test
			@DisplayName("회원의 판매 데이터양 조회를 성공한다")
			void findSellingCash() throws Exception {

				// given
				Member member = MemberFixture.createMember1();
				ReflectionTestUtils.setField(member, "sellingData", SELLING_DATA);
				memberRepository.save(member);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/members/selling-data/sold")
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").value(SELLING_DATA))
						.andDo(document("member/get-sold-data",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
										fieldWithPath("data.data").description("회원이 판매한 데이터양")
								))
						);
			}

			@Test
			@DisplayName("회원의 판매 중 + 판매 완료 데이터를 조회한다 (1.5GB 중 1.0GB 판매 완료)")
			void findAllSellingData() throws Exception {

				// given
				Member member = MemberFixture.createMember1();
				memberRepository.save(member);

				// 판매 완료된 모바일 데이터 등록 (1.0GB)
				MobileData soldMobileData = mobileDataRepository.save(
						MobileData.of(BigDecimal.valueOf(1.0), BigDecimal.valueOf(0.0), 100, false)
				);
				Product soldProduct = Product.of(
						ProductState.SOLD_OUT,
						1000,
						soldMobileData.getId(),
						ItemType.MOBILE_DATA,
						member
				);
				productRepository.save(soldProduct);

				// 판매 등록만 되어 있는 모바일 데이터 (0.5GB)
				MobileData activeMobileData = mobileDataRepository.save(
						MobileData.of(BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.5), 100, false)
				);
				Product activeProduct = Product.of(
						ProductState.ACTIVE,
						500,
						activeMobileData.getId(),
						ItemType.MOBILE_DATA,
						member
				);
				productRepository.save(activeProduct);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(get("/api/members/selling-data")
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").value(1.5)
						)
						.andDo(document("member/get-selling-data-total",
										responseFields(
												fieldWithPath("code").description("상태 코드"),
												fieldWithPath("message").description("처리 결과 메시지"),
												fieldWithPath("data").description("응답 데이터"),
												fieldWithPath("data.data").description(
														"회원이 등록한 판매 완료 및 판매 중 상품의 데이터 총합 (GB)")
										)
								)
						);
			}


		}
	}

	@Nested
	@DisplayName("회원 정보 조회 API")
	class GetMemberInfo {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("내 정보 조회 (memberId 없음)")
			void getMyMemberInfo() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				ReflectionTestUtils.setField(member, "profileImageUrl",
						"https://dapanda.org/profile/test2.jpg");
				CustomUserDetails userDetails = CustomUserDetails.from(member);
				memberRepository.save(member);
				// when & then
				mockMvc.perform(get("/api/members/info")
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").exists())
						.andExpect(jsonPath("$.message").exists())
						.andExpect(jsonPath("$.data.name").value(member.getName()))
						.andExpect(
								jsonPath("$.data.profileImageUrl").value(
										member.getProfileImageUrl()))
						.andExpect(jsonPath("$.data.joinedAt").exists())
						.andExpect(
								jsonPath("$.data.averageRating").value(member.getAverageRating()))
						.andExpect(jsonPath("$.data.reviewCount").value(member.getReviewCount()))
						.andExpect(jsonPath("$.data.tradeCount").isNumber())
						.andDo(document("member/get-my-info",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data.name").description("회원 이름"),
										fieldWithPath("data.profileImageUrl").description(
												"프로필 이미지 URL"),
										fieldWithPath("data.joinedAt").description("가입일"),
										fieldWithPath("data.averageRating").description("받은 별점"),
										fieldWithPath("data.reviewCount").description("받은 리뷰 수"),
										fieldWithPath("data.tradeCount").description("거래 수")
								)
						));
			}

			@Test
			@DisplayName("다른 회원 정보 조회 (memberId 전달)")
			void getOtherMemberInfo() throws Exception {

				// given
				Member me = memberRepository.save(MemberFixture.createMember1());
				Member other = memberRepository.save(MemberFixture.createMember2());
				ReflectionTestUtils.setField(other, "profileImageUrl",
						"https://dapanda.org/profile/test2.jpg");
				other = memberRepository.save(other);
				CustomUserDetails userDetails = CustomUserDetails.from(me);

				// when & then
				mockMvc.perform(get("/api/members/info")
								.param("memberId", String.valueOf(other.getId()))
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").exists())
						.andExpect(jsonPath("$.message").exists())
						.andExpect(jsonPath("$.data.name").value(other.getName()))
						.andExpect(jsonPath("$.data.profileImageUrl").value(
								other.getProfileImageUrl()))
						.andExpect(jsonPath("$.data.joinedAt").exists())
						.andExpect(jsonPath("$.data.averageRating").value(other.getAverageRating()))
						.andExpect(jsonPath("$.data.reviewCount").value(other.getReviewCount()))
						.andExpect(jsonPath("$.data.tradeCount").isNumber())
						.andDo(document("member/get-other-info",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data.name").description("회원 이름"),
										fieldWithPath("data.profileImageUrl").description(
												"프로필 이미지 URL"),
										fieldWithPath("data.joinedAt").description("가입일"),
										fieldWithPath("data.averageRating").description("받은 별점"),
										fieldWithPath("data.reviewCount").description("받은 리뷰 수"),
										fieldWithPath("data.tradeCount").description("거래 수")
								)
						));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("memberId로 조회 시 회원이 존재하지 않으면 400 반환")
			void getOtherMemberInfo_notFound() throws Exception {

				// given
				Member me = memberRepository.save(MemberFixture.createMember1());
				CustomUserDetails userDetails = CustomUserDetails.from(me);
				Long notExistMemberId = 99999L;

				// when & then
				mockMvc.perform(get("/api/members/info")
								.param("memberId", String.valueOf(notExistMemberId))
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
						)
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").exists())
						.andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."))
						.andDo(document("member/get-member-info-notfound",
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("에러 메시지")
								)
						));
			}

		}

	}

	@Nested
	@DisplayName("프로필 이미지 변경 API")
	class UpdateProfileImage {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("정상적으로 프로필 이미지를 변경한다")
			void updateProfileImage_success() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				CustomUserDetails userDetails = CustomUserDetails.from(member);
				String imageUrl = "https://dapanda.org/profile/test.jpg"; // 유효한 확장자
				var request = new UpdateProfileImageRequest(imageUrl);

				// when & then
				mockMvc.perform(
								MockMvcRequestBuilders.post("/api/members/profile-image")
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request))
										.with(authentication(new UsernamePasswordAuthenticationToken(
												userDetails, null, userDetails.getAuthorities()
										)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andDo(document("member/post-profile-image-success",
								requestFields(
										fieldWithPath("imageUrl").description("프로필 이미지 URL")
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
			@DisplayName("지원하지 않는 이미지 확장자면 예외를 반환한다")
			void updateProfileImage_invalidExtension() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				CustomUserDetails userDetails = CustomUserDetails.from(member);
				String imageUrl = "https://dapanda.org/profile/invalid.bmp"; // 지원하지 않는 확장자
				var request = new UpdateProfileImageRequest(imageUrl);

				// when & then
				mockMvc.perform(
								MockMvcRequestBuilders.post("/api/members/profile-image")
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request))
										.with(authentication(new UsernamePasswordAuthenticationToken(
												userDetails, null, userDetails.getAuthorities()
										)))
						)
						.andExpect(status().isBadRequest())
						.andExpect(
								jsonPath("$.code").value(ResultCode.INVALID_IMAGE_FORMAT.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.INVALID_IMAGE_FORMAT.getMessage()))
						.andDo(document("member/post-profile-image-invalid-extension-error",
								requestFields(
										fieldWithPath("imageUrl").description("프로필 이미지 URL")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("에러 메시지")
								)
						));
			}

			@Test
			@DisplayName("이미지 URL이 빈 값이면 예외를 반환한다")
			void updateProfileImage_blankImageUrl() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				CustomUserDetails userDetails = CustomUserDetails.from(member);
				var request = new UpdateProfileImageRequest("");

				// when & then
				mockMvc.perform(
								MockMvcRequestBuilders.post("/api/members/profile-image")
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request))
										.with(authentication(new UsernamePasswordAuthenticationToken(
												userDetails, null, userDetails.getAuthorities()
										)))
						)
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(ResultCode.INVALID_PARAMETER.getCode()))
						.andExpect(jsonPath("$.message").exists())
						.andDo(document("member/post-profile-image-blank-url-error",
								requestFields(
										fieldWithPath("imageUrl").description(
												"프로필 이미지 URL (비어있으면 안 됨)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("에러 메시지")
								)
						));
			}
		}
	}

}
