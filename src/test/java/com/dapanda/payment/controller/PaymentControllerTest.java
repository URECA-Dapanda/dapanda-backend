package com.dapanda.payment.controller;

import static com.dapanda.TestConstants.Member.CASH_0;
import static com.dapanda.TestConstants.Member.CASH_5000;
import static com.dapanda.TestConstants.Payment.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapanda.RedisTestContainerConfig;
import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.*;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.payment.dto.request.*;
import com.dapanda.payment.dto.response.RefundCashResponse;
import com.dapanda.payment.dto.response.TossConfirmResponse;
import com.dapanda.payment.service.TossPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@Import({TestConfig.class, RedisTestContainerConfig.class})
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("결제 컨트롤러 테스트")
class PaymentControllerTest {

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
	@Qualifier("chatPubSub")
	private RedisTemplate<String, String> redisTemplate;

	private MockMvc mockMvc;
	@MockitoBean
	private WebClient tossWebClient;
	@MockitoBean
	private TossPaymentService tossPaymentService;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);
	}

	@BeforeEach
	void cleanupDatabase() {

		entityManager.clear();

		// MySQL 초기화
		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
		jdbcTemplate.execute("TRUNCATE TABLE member");
		jdbcTemplate.execute("TRUNCATE TABLE trade");
		jdbcTemplate.execute("TRUNCATE TABLE product");
		jdbcTemplate.execute("TRUNCATE TABLE payment");
		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

		// Redis 초기화
		redisTemplate.delete(redisTemplate.keys("*"));
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
						.andDo(document("payments/save-amount",
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
						.andDo(document("payments/verify-amount",
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
						.andDo(document("payments/verify-amount-mismatch-error",
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
	@DisplayName("캐시 충전 API")
	class Confirm {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("결제 승인 요청에 성공하면 잔액이 갱신되고 승인 응답을 반환한다")
			void chargeCashTest() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());

				ChargeCashRequest request = new ChargeCashRequest("payKey123", "order123", 10000);
				TossConfirmResponse confirmResponse = new TossConfirmResponse(request.orderId(),
						request.paymentKey(), request.amount(), APPROVED_AT);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				given(tossPaymentService.confirmPayment(request)).willReturn(confirmResponse);

				// when & then
				mockMvc.perform(post("/api/payments/charge")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andDo(print())
						.andDo(document("payments/charge-cash",
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
				Member member = memberRepository.save(MemberFixture.createMember1());

				ChargeCashRequest request = new ChargeCashRequest("payKey123", "order123", 5000);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				given(tossPaymentService.confirmPayment(request)).willThrow(
						new GlobalException(ResultCode.FAIL_PAYMENT_APPROVAL));

				// when & then
				mockMvc.perform(post("/api/payments/charge")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(
								ResultCode.FAIL_PAYMENT_APPROVAL.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.FAIL_PAYMENT_APPROVAL.getMessage()))
						.andDo(document("payments/charge-cash-payment-error",
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
				Member member = memberRepository.save(MemberFixture.createMember1());

				String paymentKey = "payKey123";
				String orderId = "order123";
				int amount = 0;

				ChargeCashRequest request = new ChargeCashRequest(paymentKey, orderId, amount);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(post("/api/payments/charge")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(
								ResultCode.INVALID_PAYMENT_AMOUNT.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.INVALID_PAYMENT_AMOUNT.getMessage()))
						.andDo(document("payments/charge-cash-payment-invalid-amount-error",
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

	@Nested
	@DisplayName("캐시 환불 API")
	class UpdateMobileData {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("캐시 환불을 성공한다")
			void refundCashTest() throws Exception {

				// given
				Member member = MemberFixture.createMember1();
				ReflectionTestUtils.setField(member, "cash", CASH_5000);
				memberRepository.save(member);

				RefundCashRequest request = new RefundCashRequest(REQUEST_ID, REFUND_AMOUNT_3000);
				RefundCashResponse response = RefundCashResponse.of(REFUND_AMOUNT_3000,
						CASH_5000 - REFUND_AMOUNT_3000);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(post("/api/payments/refund")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.refundPrice").value(response.getRefundPrice()))
						.andExpect(jsonPath("$.data.remainCash").value(response.getRemainCash()))
						.andDo(print())
						.andDo(document("payments/refund-cash",
								requestFields(
										fieldWithPath("requestId").description(
												"환불 요청 아이디 (임의의 아이디 생성)"),
										fieldWithPath("refundAmount").description("환불 금액")
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("처리 결과 메시지"),
										fieldWithPath("data.refundPrice").description("환불된 금액"),
										fieldWithPath("data.remainCash").description("환불 후 남은 캐시")
								)
						));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("요청 아이디가 유효하지 않을 경우 예외를 던진다")
			public void throwsExceptionWhenRequestIdInvalid() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				ReflectionTestUtils.setField(member, "cash", CASH_5000);

				RefundCashRequest request = new RefundCashRequest(INVALID_REQUEST_ID,
						REFUND_AMOUNT_3000);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(post("/api/payments/refund")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isBadRequest())
						.andExpect(
								jsonPath("$.code").value(ResultCode.INVALID_REQUEST_ID.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.INVALID_REQUEST_ID.getMessage()))
						.andDo(document("payments/refund-cash-invalid-request-id-error",
								requestFields(
										fieldWithPath("requestId").description("환불 요청 아이디"),
										fieldWithPath("refundAmount").description("환불 금액")
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								)
						));
			}

			@Test
			@DisplayName("요청 아이디가 존재할 경우 예외를 던진다")
			public void throwsExceptionWhenRequestIdIsExists() throws Exception {

				// given
				Member member = memberRepository.save(MemberFixture.createMember1());
				ReflectionTestUtils.setField(member, "cash", CASH_5000);

				redisTemplate.opsForValue().set(DUPLICATE_REQUEST_ID, "1");

				RefundCashRequest request = new RefundCashRequest(REQUEST_ID, REFUND_AMOUNT_3000);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(post("/api/payments/refund")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.code").value(ResultCode.DUPLICATE_REQUEST.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.DUPLICATE_REQUEST.getMessage()))
						.andDo(document("payments/refund-cash-duplicate-request-id-error",
								requestFields(
										fieldWithPath("requestId").description("환불 요청 아이디"),
										fieldWithPath("refundAmount").description("환불 금액")
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								)
						));
			}

			@Test
			@DisplayName("보유한 캐시가 요청한 환불 캐시양보다 적으면 예외를 던진다")
			public void throwsExceptionWhenRequestAmountIsGreaterThanCash() throws Exception {

				// given
				Member member = MemberFixture.createMember1();
				ReflectionTestUtils.setField(member, "cash", CASH_0);
				memberRepository.save(member);

				RefundCashRequest request = new RefundCashRequest(REQUEST_ID, REFUND_AMOUNT_3000);

				CustomUserDetails userDetails = CustomUserDetails.from(member);

				// when & then
				mockMvc.perform(post("/api/payments/refund")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isBadRequest())
						.andExpect(
								jsonPath("$.code").value(ResultCode.INVALID_CASH_AMOUNT.getCode()))
						.andExpect(jsonPath("$.message").value(
								ResultCode.INVALID_CASH_AMOUNT.getMessage()))
						.andDo(document("payments/refund-cash-invalid-cash-amount-error",
								requestFields(
										fieldWithPath("requestId").description("환불 요청 아이디"),
										fieldWithPath("refundAmount").description("환불 금액")
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								)
						));
			}

//			@Test
//			@DisplayName("캐시 환불 요청이 지연될 경우 예외를 던진다")
//			public void throwsExceptionWhenRequestDelay() throws Exception {
//
//				// given
//				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
//				ReflectionTestUtils.setField(member, "cash", CASH_5000);
//				memberRepository.save(member); // 실제 DB에 저장해야 @Transactional for update가 동작
//
//				CustomUserDetails userDetails = CustomUserDetails.from(member);
//
//				RefundCashRequest request = new RefundCashRequest(REQUEST_ID, REFUND_AMOUNT_3000);
//
//				// 트랜잭션 A: 락을 점유한 채 오래 점유
//				ExecutorService executor = Executors.newFixedThreadPool(2);
//				CountDownLatch latch = new CountDownLatch(1);
//
//				executor.submit(() -> {
//					TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
//					txTemplate.executeWithoutResult(status -> {
//						memberRepository.findByIdForUpdate(member.getId()); // 락 점유
//						latch.countDown(); // 다른 스레드에게 시작 신호
//						try {
//							Thread.sleep(40000000); // 오래 점유하여 다른 스레드 타임아웃 유도
//						} catch (InterruptedException e) {
//							throw new RuntimeException(e);
//						}
//					});
//				});
//
//				// 락 점유 시작까지 기다림
//				latch.await();
//
//				// when: 락에 접근하여 타임아웃 발생 예상
//				MvcResult result = mockMvc.perform(post("/api/payments/refund")
//								.contentType(MediaType.APPLICATION_JSON)
//								.content(objectMapper.writeValueAsString(request))
//								.with(authentication(new UsernamePasswordAuthenticationToken(
//										CustomUserDetails.from(member), null,
//										userDetails.getAuthorities()))))
//						.andExpect(status().isRequestTimeout())
//						.andExpect(jsonPath("$.code").value(ResultCode.REQUEST_TIMEOUT.getCode()))
//						.andExpect(jsonPath("$.message").value(
//								ResultCode.REQUEST_TIMEOUT.getMessage()))
//						.andReturn();
//			}
		}
	}
}
