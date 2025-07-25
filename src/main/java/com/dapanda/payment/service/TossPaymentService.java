package com.dapanda.payment.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.payment.dto.request.ChargeCashRequest;
import com.dapanda.payment.dto.response.TossConfirmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class TossPaymentService {

	private final WebClient tossWebClient;

	public TossConfirmResponse confirmPayment(ChargeCashRequest request) {

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

		return response;
	}

}
