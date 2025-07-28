package com.dapanda.payment.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.payment.dto.request.ChargeCashRequest;
import com.dapanda.payment.dto.request.RefundCashRequest;
import com.dapanda.payment.dto.response.*;
import com.dapanda.payment.entity.Payment;
import com.dapanda.payment.repository.PaymentRepository;
import com.dapanda.trade.entity.Trade;
import com.dapanda.trade.entity.TradeType;
import com.dapanda.trade.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

	private final MemberRepository memberRepository;
	private final PaymentRepository paymentRepository;
	private final TradeRepository tradeRepository;
	private final TossPaymentService tossPaymentService;
	private final RedisTemplate<String, String> redisTemplate;

	@Transactional
	public ChargeCashResponse chargeCash(Long memberId, ChargeCashRequest request) {

		Member member = memberRepository.findByIdForUpdate(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		if (!isValidAmount(request.amount())) {
			throw new GlobalException(ResultCode.INVALID_PAYMENT_AMOUNT);
		}

		TossConfirmResponse response = tossPaymentService.confirmPayment(request);

		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		LocalDateTime approvedAt = LocalDateTime.parse(response.approvedAt(), formatter);

		Payment payment = paymentRepository.save(
				Payment.of(response.paymentKey(), response.totalAmount(),
						approvedAt, member));

		tradeRepository.save(Trade.of(request.amount(), TradeType.CHARGE, member));

		return ChargeCashResponse.of(payment.getId(), response.totalAmount());
	}

	// TODO: 성능 고도화 시 동시성 처리(락)
	@Transactional
	public void updateCash(Long memberId, int amount) {

		Member member = memberRepository.findByIdForUpdate(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		member.addCash(amount);
	}

	/**
	 * 1. 캐시 환불 요청
	 * 2. 회원 ID로 비관적 락 걸고 조회
	 * 3. 보유 캐시 유효성 검증 (부족 시 예외)
	 * 4. 중복 요청 검증 (requestId 기반, Redis 또는 DB)
	 * 5. 락 타임아웃 예외 처리 or 재시도 처리
	 * 6. 가상 환불 처리
	 * 7. Trade 테이블에 환불 내역 기록 (type=REFUND)
	 * 8. 알림, 로그 기록 (이벤트 발행은 트랜잭션 이후)
	 * 9. Response: 환불 금액, 남은 캐시 잔액
	 */
	@Transactional
	public RefundCashResponse refundCash(Long memberId, RefundCashRequest request) {

		try {
			if (request.requestId() == null || request.requestId().isBlank()) {
				throw new GlobalException(ResultCode.INVALID_REQUEST_ID);
			}

			String requestKey = "refund:" + request.requestId();
			Boolean isDuplicate = redisTemplate.opsForValue()
					.setIfAbsent(requestKey, "1", Duration.ofMinutes(5));

			// 중복된 요청인지 검증
			if (Boolean.FALSE.equals(isDuplicate)) {
				throw new GlobalException(ResultCode.DUPLICATE_REQUEST);
			}

			Member member = memberRepository.findByIdForUpdate(memberId).orElseThrow();

			if (member.getCash() < request.refundAmount()) {
				throw new GlobalException(ResultCode.INVALID_CASH_AMOUNT);
			}

			member.deductCash(request.refundAmount());

			// 가상 환불 로직 (실제 구현 X)

			Trade trade = Trade.of(request.refundAmount(), TradeType.REFUND, member);
			tradeRepository.save(trade);

			return RefundCashResponse.of(request.refundAmount(), member.getCash());
		} catch (PessimisticLockingFailureException e) {
			throw new GlobalException(ResultCode.REQUEST_TIMEOUT);
		}
	}

	private boolean isValidAmount(int amount) {

		return amount > 0;
	}
}
