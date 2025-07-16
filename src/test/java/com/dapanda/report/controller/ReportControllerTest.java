package com.dapanda.report.controller;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.Product;
import com.dapanda.product.entity.ProductFixture;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.report.dto.request.CreateReportRequest;
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
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static com.dapanda.TestConstants.Member.USER_DETAILS_MEMBER_ID;
import static com.dapanda.TestConstants.Report.REASON;
import static com.dapanda.TestConstants.Report.REPORT_TARGET_CATEGORY_PRODUCT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("신고 컨트롤러 테스트")
class ReportControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManager entityManager;

	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ProductRepository productRepository;


	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);

		cleanupDatabase();
	}

	private void cleanupDatabase() {

		entityManager.clear();

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

		jdbcTemplate.execute("TRUNCATE TABLE review");
		jdbcTemplate.execute("TRUNCATE TABLE member");

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
	}

	@Nested
	@DisplayName("신고 생성 API")
	class CreateReport {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("유효한 요청이면 생성된 신고 아이디를 반환한다")
			public void createReportTest() throws Exception {

				//given
				CreateReportRequest request = new CreateReportRequest(REASON, REPORT_TARGET_CATEGORY_PRODUCT);

				Member reporter = memberRepository.save(MemberFixture.createMember1());
				Member reportedMember = memberRepository.save(MemberFixture.createMember2());

				Product product = productRepository.save(ProductFixture.createProduct1(reportedMember));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(reporter.getId());

				//when & then
				mockMvc.perform(post("/api/report/{targetId}", product.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								)))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.reportId").exists())
						.andDo(document("report/create-report",
										pathParameters(
												parameterWithName("targetId").description("신고 대상 아이디 (필수)")
										),
										requestFields(
												fieldWithPath("reason").description("신고 사유 (필수)"),
												fieldWithPath("targetCategory").description("신고 대상 아이디 유형 (필수)")
										),
										responseFields(
												fieldWithPath("code").description("상태 코드"),
												fieldWithPath("message").description("처리 결과 메시지"),
												fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
												fieldWithPath("data.reportId").description("생성된 신고 아이디")
										)
								)
						);
			}
		}
	}
}
