package com.dapanda.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberRole;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.payment.dto.ConfirmPaymentResponse;
import com.dapanda.payment.dto.request.TossConfirmRequest;
import com.dapanda.payment.entity.Payment;
import com.dapanda.payment.repository.PaymentRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 서비스 테스트")
class PaymentServiceTest {

	private WebClient webClient;
	private MockWebServer mockWebServer;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@InjectMocks
	private PaymentService paymentService;

	@BeforeEach
	void setUp() throws java.io.IOException {

		mockWebServer = new MockWebServer();
		mockWebServer.start();
		String mockBaseUrl = mockWebServer.url("/").toString();
		webClient = WebClient.builder().baseUrl(mockBaseUrl).build();
		paymentService = new PaymentService(memberRepository, paymentRepository, webClient);
	}

	@AfterEach
	void tearDown() throws java.io.IOException {

		mockWebServer.shutdown();
	}

	@Nested
	@DisplayName("결제 승인")
	class ConfirmPayment {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("결제 승인을 성공한다")
			void confirmPaymentTest() {

				// given
				String responseJson = """
						    {
						      "orderId": "testOrderId",
						      "paymentKey": "testPaymentKey",
						      "totalAmount": 10000,
						      "approvedAt": "2024-01-01T00:00:00Z"
						    }
						""";

				TossConfirmRequest request = new TossConfirmRequest("testPaymentKey", "testOrderId",
						10000);

				Member mockMember = Member.ofLocalMember("abc@gmail.com", "홍길동", "abc1234",
						OAuthProvider.LOCAL, MemberRole.ROLE_MEMBER);
				Payment mockedPayment = Payment.of("testPaymentKey", 10000,
						OffsetDateTime.parse("2024-01-01T00:00:00Z").toLocalDateTime(),
						mockMember);
				ReflectionTestUtils.setField(mockedPayment, "id", 1L);

				when(paymentRepository.save(any())).thenReturn(mockedPayment);
				when(memberRepository.findById(1L)).thenReturn(Optional.of(mockMember));

				mockWebServer.enqueue(new MockResponse()
						.setResponseCode(200)
						.setBody(responseJson)
						.addHeader("Content-Type", "application/json"));

				// when
				ConfirmPaymentResponse response = paymentService.confirmPayment(1L, request);

				// then
				assertEquals(1L, response.getPaymentId());
				assertEquals(10000, response.getTotalAmount());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("Toss API가 400 에러를 반환하면 예외가 발생한다")
			void confirmPaymentFailIfTossReturnsErrorTest() {

				// given
				TossConfirmRequest request = new TossConfirmRequest("invalid_key", "test_orderId",
						10000);

				Member mockMember = Member.ofLocalMember("abc@gmail.com", "홍길동", "abc1234",
						OAuthProvider.LOCAL, MemberRole.ROLE_MEMBER);

				when(memberRepository.findById(1L)).thenReturn(Optional.of(mockMember));

				mockWebServer.enqueue(new MockResponse()
						.setResponseCode(400)
						.setBody("{\"message\": \"Invalid paymentKey\"}")
						.addHeader("Content-Type", "application/json"));

				// when
				GlobalException exception = assertThrows(GlobalException.class, () -> {
					paymentService.confirmPayment(1L, request);
				});

				// then
				assertEquals(ResultCode.FAIL_PAYMENT_APPROVAL, exception.getResultCode());
			}
		}

		@Nested
		@DisplayName("캐시 충전")
		class UpdateCash {

			@Nested
			@DisplayName("성공 케이스")
			class Success {

				@Test
				@DisplayName("캐시 충전을 성공한다")
				void updateCashTest() {

					// given
					Long memberId = 1L;
					int amount = 5000;

					Member mockMember = Member.ofLocalMember("abc@gmail.com", "홍길동", "abc1234",
							OAuthProvider.LOCAL, MemberRole.ROLE_MEMBER);

					when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));

					// when
					paymentService.updateCash(memberId, amount);

					// then
					assertEquals(5000, mockMember.getCash());
				}
			}

			@Nested
			@DisplayName("실패 케이스")
			class Fail {

				@ParameterizedTest
				@ValueSource(ints = {-3000, 0})
				@DisplayName("결제 금액이 유효하지 않으면 캐시 충전을 실패한다")
				void updateCashTest(int amount) {

					// given
					Long memberId = 1L;

					Member mockMember = Member.ofLocalMember("abc@gmail.com", "홍길동", "abc1234",
							OAuthProvider.LOCAL, MemberRole.ROLE_MEMBER);

					when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));

					// when
					GlobalException exception = assertThrows(GlobalException.class, () -> {
						paymentService.updateCash(memberId, amount);
					});

					// then
					assertEquals(ResultCode.INVALID_PAYMENT_AMOUNT, exception.getResultCode());
				}
			}
		}
	}
}