package com.dapanda.plan.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.plan.entity.*;
import com.dapanda.plan.repository.PlanRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("플랜 컨트롤러 테스트")
class PlanControllerTest {

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
	private PlanRepository planRepository;
	private MockMvc mockMvc;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);
		cleanupDatabase();
	}

	private void cleanupDatabase() {

		entityManager.clear();
		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
		jdbcTemplate.execute("TRUNCATE TABLE plan");
		jdbcTemplate.execute("TRUNCATE TABLE member");
		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
	}

	@Nested
	@DisplayName("나의 플랜 데이터 조회 API")
	class GetMyMobileDataInfo {

		private Member member;
		private CustomUserDetails userDetails;

		@BeforeEach
		void setUp() {

			member = memberRepository.save(MemberFixture.createMember1());
			userDetails = CustomUserDetails.from(member);
		}

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("정상적으로 나의 플랜 데이터를 조회한다")
			void getMyMobileDataInfo_success() throws Exception {

				// given
				Plan plan = planRepository.save(Plan.of(
						"청년 Value 베이직",
						BigDecimal.valueOf(30.0),
						15000,
						PlanCategory._5G,
						AgeGroup.YOUTH,
						member
				));

				// when & then
				mockMvc.perform(get("/api/plans/my-data")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(0))
						.andExpect(jsonPath("$.data.name").value("청년 Value 베이직"))
						.andExpect(jsonPath("$.data.providingDataAmount").value(30.0))
						.andExpect(jsonPath("$.data.monthlyPrice").value(15000))
						.andDo(document("plans/get-my-plan-info",
								responseFields(
										// (CommonResponse에 맞게 필드 설명 작성)
										// 예시:
										org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath(
												"code").description("상태 코드"),
										org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath(
												"message").description("처리 결과 메시지"),
										org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath(
												"data.name").description("플랜 이름"),
										org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath(
												"data.providingDataAmount").description("제공 데이터 양"),
										org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath(
												"data.monthlyPrice").description("월 요금")
								)
						));

				// 실제 서비스로직 검증 (Optional)
				var result = planRepository.findByMemberId(member.getId()).orElseThrow();
				assertThat(result.getProvidingDataAmount()).isEqualByComparingTo(
						BigDecimal.valueOf(30.0));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("플랜이 없는 회원이 요청하면 예외를 반환한다")
			void getMyMobileDataInfo_fail_noPlan() throws Exception {

				// given: member만 있고 plan 없음

				// when & then
				mockMvc.perform(get("/api/plans/my-data")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								)))
								.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest())
						.andDo(document("plan/get-my-plan-info-no-plan-error",
								responseFields(
										org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath(
												"code").description("상태 코드"),
										org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath(
												"message").description("에러 메시지")
								)
						));
			}
		}
	}
}
