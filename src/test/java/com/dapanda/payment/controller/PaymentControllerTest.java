package com.dapanda.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapanda.RestDocsConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.MemberRole;
import com.dapanda.payment.dto.ConfirmPaymentResponse;
import com.dapanda.payment.dto.request.AmountRequest;
import com.dapanda.payment.dto.request.TossConfirmRequest;
import com.dapanda.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@Import({RestDocsConfig.class})
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("결제 컨트롤러 테스트")
class PaymentControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	private MockMvc mockMvc;

	@MockitoBean
	private WebClient tossWebClient;

	@MockitoBean
	private PaymentService paymentService;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = RestDocsConfig.createMockMvc(context, restDocumentation);
	}

	@BeforeEach
	void setUpSecurityContext() {
		CustomUserDetails userDetails = new CustomUserDetails(
				1L, "user@example.com", "password", "LOCAL", MemberRole.ROLE_MEMBER
		);

		UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(userDetails, null,
						userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(auth);
	}


	@Nested
	@DisplayName("결제 금액 임시 저장 API")
	class SaveAmount {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("주문 아이디와 결제 금액을 세션에 임시 저장한다")
			public void saveAmountTest() throws Exception {

				//given
				String orderId = "testOrderId";
				int amount = 10000;

				AmountRequest request = new AmountRequest(orderId, amount);

				//when & then
				mockMvc.perform(post("/api/payments/save-amount")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andDo(document("save-amount",
								requestFields(
										fieldWithPath("orderId").description(
												"주문 아이디 (필수)"),
										fieldWithPath("amount").description(
												"결제 금액 (필수)")
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
	@DisplayName("결제 금액 검증 API")
	class VerifyAmount {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("세션에 저장된 금액과 요청 금액을 검증한다")
			public void verifyAmountTest() throws Exception {

				// given
				String orderId = "testOrderId";
				int savedAmount = 10000;
				int requestAmount = 10000;

				// 세션에 값 저장
				MockHttpSession session = new MockHttpSession();
				session.setAttribute(orderId, savedAmount);

				AmountRequest request = new AmountRequest(orderId, requestAmount);

				// when & then
				mockMvc.perform(post("/api/payments/verify-amount")
								.session(session)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andDo(document("verify-amount",
								requestFields(
										fieldWithPath("orderId").description("주문 아이디 (필수)"),
										fieldWithPath("amount").description("검증할 결제 금액 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								)));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("세션에 저장된 금액과 요청 금액이 다르면 실패한다")
			public void verifyAmountTest() throws Exception {

				// given
				String orderId = "testOrderId";
				int savedAmount = 5000;
				int requestAmount = 10000;

				// 세션에 값 저장
				MockHttpSession session = new MockHttpSession();
				session.setAttribute(orderId, savedAmount);

				AmountRequest request = new AmountRequest(orderId, requestAmount);

				// when & then
				mockMvc.perform(post("/api/payments/verify-amount")
								.session(session)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(
								ResultCode.PAYMENT_AMOUNT_MISMATCH.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.PAYMENT_AMOUNT_MISMATCH.getMessage()))
						.andDo(document("verify-amount-mismatch-error",
								requestFields(
										fieldWithPath("orderId").description("주문 아이디 (필수)"),
										fieldWithPath("amount").description("검증할 결제 금액 (필수)")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								)));
			}
		}
	}

	@Nested
	@DisplayName("결제 승인 API")
	class Confirm {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("결제 승인 요청에 성공하면 잔액이 갱신되고 승인 응답을 반환한다")
			void confirmTest() throws Exception {

				// given
				TossConfirmRequest request = new TossConfirmRequest("payKey123", "order123", 10000);
				ConfirmPaymentResponse response = ConfirmPaymentResponse.from(1L, 10000);

				given(paymentService.confirmPayment(eq(1L), any(TossConfirmRequest.class)))
						.willReturn(response);

				willDoNothing().given(paymentService).updateCash(eq(1L), eq(10000));

				// when & then
				mockMvc.perform(post("/api/payments/confirm")
								.principal(() -> "user")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andDo(document("confirm-payment",
								requestFields(
										fieldWithPath("paymentKey").description("토스 결제 키"),
										fieldWithPath("orderId").description("주문 아이디"),
										fieldWithPath("amount").description("결제 금액")
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data.paymentId").description("결제 아이디"),
										fieldWithPath("data.totalAmount").description("총 결제 금액")
								)));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("토스 결제 승인 요청 실패 시 예외를 반환한다")
			public void confirmFailByWebClientTest() throws Exception {

				// given
				TossConfirmRequest request = new TossConfirmRequest("payKey123", "order123", 5000);
				Long memberId = 1L;

				given(paymentService.confirmPayment(eq(memberId), any(TossConfirmRequest.class)))
						.willThrow(new GlobalException(ResultCode.FAIL_PAYMENT_APPROVAL));

				// when & then
				mockMvc.perform(post("/api/payments/confirm")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(
								ResultCode.FAIL_PAYMENT_APPROVAL.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.FAIL_PAYMENT_APPROVAL.getMessage()))
						.andDo(document("confirm-payment-error",
								requestFields(
										fieldWithPath("paymentKey").description("토스 결제 키"),
										fieldWithPath("orderId").description("주문 아이디"),
										fieldWithPath("amount").description("결제 금액")
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								)));
			}

			@Test
			@DisplayName("결제 금액이 0 이하인 경우 실패한다")
			void confirmFailIfInvalidAmount() throws Exception {

				// given
				Long memberId = 1L;
				String paymentKey = "payKey123";
				String orderId = "order123";
				int amount = 0;

				TossConfirmRequest request = new TossConfirmRequest(paymentKey, orderId, amount);
				ConfirmPaymentResponse response = ConfirmPaymentResponse.from(1L, amount);

				given(paymentService.confirmPayment(eq(memberId), any()))
						.willReturn(response);

				willThrow(new GlobalException(ResultCode.INVALID_PAYMENT_AMOUNT))
						.given(paymentService).updateCash(eq(memberId), eq(0));

				// when & then
				mockMvc.perform(post("/api/payments/confirm")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
						)
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(
								ResultCode.INVALID_PAYMENT_AMOUNT.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.INVALID_PAYMENT_AMOUNT.getMessage()))
						.andDo(document("confirm-payment-invalid-amount-error",
								requestFields(
										fieldWithPath("paymentKey").description("토스 결제 키"),
										fieldWithPath("orderId").description("주문 아이디"),
										fieldWithPath("amount").description("결제 금액")
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								)));
			}
		}
	}
}
