package com.dapanda.payment.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.payment.dto.request.TossConfirmRequest;
import com.dapanda.payment.dto.response.TossConfirmResponse;
import com.dapanda.payment.entity.Payment;
import com.dapanda.payment.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final WebClient tossWebClient;

    public TossConfirmResponse confirmPayment(Long memberId, TossConfirmRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

        TossConfirmResponse response;

        try {
            response = tossWebClient.post()
                    .uri("/v1/payments/confirm")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TossConfirmResponse.class)
                    .block();

            if (response == null) {
                throw new GlobalException(ResultCode.FAIL_PAYMENT_APPROVAL);
            }
        } catch (WebClientResponseException e) {
            log.error("결제 승인 실패: {}", e.getResponseBodyAsString());
            throw new GlobalException(ResultCode.FAIL_PAYMENT_APPROVAL);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime approvedAt = LocalDateTime.parse(response.approvedAt(), formatter);

        paymentRepository.save(
                Payment.of(response.paymentKey(), response.totalAmount(),
                        approvedAt, member));

        return response;
    }

    // TODO: 성능 고도화 시 동시성 처리(락)
    public void updateCash(Long memberId, int amount) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

        if (!isValidAmount(amount)) {
            throw new GlobalException(ResultCode.INVALID_PAYMENT_AMOUNT);
        }

        member.addCash(amount);
    }

    private boolean isValidAmount(int amount) {

        return amount > 0;
    }
}