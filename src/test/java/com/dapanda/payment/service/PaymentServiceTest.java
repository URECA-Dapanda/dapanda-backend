package com.dapanda.payment.service;

import static com.dapanda.TestConstants.Member.*;
import static com.dapanda.TestConstants.Payment.*;
import static com.dapanda.common.exception.ResultCode.FAIL_PAYMENT_APPROVAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.payment.dto.request.ChargeCashRequest;
import com.dapanda.payment.dto.request.RefundCashRequest;
import com.dapanda.payment.dto.response.*;
import com.dapanda.payment.entity.Payment;
import com.dapanda.payment.repository.PaymentRepository;
import com.dapanda.trade.entity.Trade;
import com.dapanda.trade.repository.TradeRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
	private TradeRepository tradeRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private TossPaymentService tossPaymentService;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private PaymentService paymentService;

	@BeforeEach
	void setUp() throws java.io.IOException {

		mockWebServer = new MockWebServer();
		mockWebServer.start();
		String mockBaseUrl = mockWebServer.url("/").toString();
		webClient = WebClient.builder().baseUrl(mockBaseUrl).build();
		paymentService = new PaymentService(memberRepository, paymentRepository, tradeRepository,
				tossPaymentService, redisTemplate);
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
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				ChargeCashRequest request = new ChargeCashRequest("paymentKye123", "orderId123",
						CHARGE_AMOUNT_3000);
				TossConfirmResponse confirmResponse = new TossConfirmResponse("orderId123",
						"paymentKye123",
						TOTAL_AMOUNT_3000, APPROVED_AT);

				Payment savedPayment = Payment.of(confirmResponse.paymentKey(),
						confirmResponse.totalAmount(),
						LocalDateTime.parse(APPROVED_AT, DateTimeFormatter.ISO_DATE_TIME), member);
				ReflectionTestUtils.setField(savedPayment, "id", PAYMENT_ID);

				given(memberRepository.findByIdForUpdate(MEMBER_ID)).willReturn(
						Optional.of(member));
				given(tossPaymentService.confirmPayment(request)).willReturn(confirmResponse);
				given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);
				given(tradeRepository.save(any())).willReturn(
						mock(Trade.class));

				// when
				ChargeCashResponse result = paymentService.chargeCash(MEMBER_ID, request);

				// then
				assertThat(result.getPaymentId()).isEqualTo(PAYMENT_ID);
				assertThat(result.getTotalAmount()).isEqualTo(TOTAL_AMOUNT_3000);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("Toss API가 400 에러를 반환하면 예외가 발생한다")
			void confirmPaymentFailIfTossReturnsErrorTest() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);

				ChargeCashRequest request = new ChargeCashRequest("paymentKye123", "orderId123",
						CHARGE_AMOUNT_3000);

				given(memberRepository.findByIdForUpdate(MEMBER_ID)).willReturn(
						Optional.of(member));
				given(tossPaymentService.confirmPayment(request)).willThrow(
						new GlobalException(ResultCode.FAIL_PAYMENT_APPROVAL));

				// when & then
				assertThatThrownBy(() -> paymentService.chargeCash(MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(FAIL_PAYMENT_APPROVAL.getMessage());
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
				void chargeCashTest() throws Exception {

					// given
					Member member = MemberFixture.createMember1WithId(MEMBER_ID);

					ChargeCashRequest request = new ChargeCashRequest("paymentKye123", "orderId123",
							CHARGE_AMOUNT_3000);
					given(memberRepository.findByIdForUpdate(MEMBER_ID)).willReturn(
							Optional.of(member));
					given(tossPaymentService.confirmPayment(request)).willReturn(
							new TossConfirmResponse("orderId123", "paymentKey", CHARGE_AMOUNT_3000,
									APPROVED_AT));
					given(paymentRepository.save(any(Payment.class))).willReturn(
							Payment.of("paymentKey", CHARGE_AMOUNT_3000,
									LocalDateTime.of(2025, 7, 29, 3, 35), member));

					// when
					paymentService.chargeCash(MEMBER_ID, request);

					// then
					Member updateMember = memberRepository.findByIdForUpdate(MEMBER_ID)
							.orElseThrow();

					assertThat(updateMember.getCash()).isEqualTo(CHARGE_AMOUNT_3000);
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
					Member member = MemberFixture.createMember1WithId(MEMBER_ID);

					ChargeCashRequest request = new ChargeCashRequest("paymentKye123", "orderId123",
							amount);

					given(memberRepository.findByIdForUpdate(MEMBER_ID)).willReturn(
							Optional.of(member));

					// when & then
					assertThatThrownBy(() -> paymentService.chargeCash(MEMBER_ID, request))
							.isInstanceOf(GlobalException.class)
							.hasMessage(ResultCode.INVALID_PAYMENT_AMOUNT.getMessage());
				}
			}
		}
	}

	@Nested
	@DisplayName("캐시 환불")
	class UpdateMobileData {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("캐시 환불을 성공한다")
			public void refundCashTest() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				ReflectionTestUtils.setField(member, "cash", CASH_5000);

				RefundCashRequest request = new RefundCashRequest(REQUEST_ID, REFUND_AMOUNT_3000);

				given(redisTemplate.opsForValue()).willReturn(valueOperations);
				given(valueOperations.setIfAbsent(any(), any(), any())).willReturn(true);
				given(memberRepository.findByIdForUpdate(MEMBER_ID)).willReturn(
						Optional.of(member));

				// when
				RefundCashResponse response = paymentService.refundCash(MEMBER_ID,
						request);

				// then
				assertThat(response.getRefundPrice()).isEqualTo(REFUND_AMOUNT_3000);
				assertThat(response.getRemainCash()).isEqualTo(CASH_5000 - REFUND_AMOUNT_3000);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("요청 아이디가 유효하지 않을 경우 예외를 던진다")
			public void throwsExceptionWhenRequestIdInvalid() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				ReflectionTestUtils.setField(member, "cash", CASH_5000);

				RefundCashRequest request = new RefundCashRequest(INVALID_REQUEST_ID,
						REFUND_AMOUNT_3000);

				// when & then
				assertThatThrownBy(() -> paymentService.refundCash(MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INVALID_REQUEST_ID.getMessage());
			}

			@Test
			@DisplayName("요청 아이디가 존재할 경우 예외를 던진다")
			public void throwsExceptionWhenRequestIdIsExists() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				ReflectionTestUtils.setField(member, "cash", CASH_5000);

				RefundCashRequest request = new RefundCashRequest(REQUEST_ID, REFUND_AMOUNT_3000);

				given(redisTemplate.opsForValue()).willReturn(valueOperations);
				given(valueOperations.setIfAbsent("refund:" + REQUEST_ID, "1",
						Duration.ofMinutes(5))).willReturn(false);

				// when & then
				assertThatThrownBy(() -> paymentService.refundCash(MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.DUPLICATE_REQUEST.getMessage());
			}

			@Test
			@DisplayName("보유한 캐시가 요청한 환불 캐시양보다 적으면 예외를 던진다")
			public void throwsExceptionWhenRequestAmountIsGreaterThanCash() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				ReflectionTestUtils.setField(member, "cash", CASH_0);

				RefundCashRequest request = new RefundCashRequest(REQUEST_ID, REFUND_AMOUNT_3000);

				given(redisTemplate.opsForValue()).willReturn(valueOperations);
				given(valueOperations.setIfAbsent(any(), any(), any())).willReturn(true);
				given(memberRepository.findByIdForUpdate(MEMBER_ID)).willReturn(
						Optional.of(member));

				// when & then
				assertThatThrownBy(() -> paymentService.refundCash(MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INVALID_CASH_AMOUNT.getMessage());
			}

			@Test
			@DisplayName("캐시 환불 요청이 지연될 경우 예외를 던진다")
			public void throwsExceptionWhenRequestDelay() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				ReflectionTestUtils.setField(member, "cash", CASH_5000);

				RefundCashRequest request = new RefundCashRequest(REQUEST_ID, REFUND_AMOUNT_3000);

				given(redisTemplate.opsForValue()).willReturn(valueOperations);
				given(valueOperations.setIfAbsent(any(), any(), any())).willReturn(true);
				given(memberRepository.findByIdForUpdate(MEMBER_ID))
						.willThrow(new PessimisticLockingFailureException("lock timeout"));

				// when & then
				assertThatThrownBy(() -> paymentService.refundCash(MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.REQUEST_TIMEOUT.getMessage());
			}
		}
	}
}
