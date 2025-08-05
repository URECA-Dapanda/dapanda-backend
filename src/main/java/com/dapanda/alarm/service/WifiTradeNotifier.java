package com.dapanda.alarm.service;

import com.dapanda.alarm.event.WifiTradeEvent;
import com.dapanda.member.entity.Member;
import com.dapanda.product.entity.Wifi;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.trade.entity.Trade;
import com.dapanda.trade.entity.TradeDetails;
import com.dapanda.trade.repository.TradeDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WifiTradeNotifier {

	private final TradeDetailsRepository tradeDetailsRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final ProductRepository productRepository;

	public void notifyOngoingTradeIfExists(Member member) {
		log.info("🛎️ [알림] notifyOngoingTradeIfExists 호출됨 → memberId={}", member.getId());

		List<TradeDetails> tradeDetailsList = tradeDetailsRepository.findOngoingWifiTradeDetailsByMemberId(
				member.getId());

		if (tradeDetailsList.isEmpty()) {
			log.info("❌ 진행 중인 와이파이 거래 없음 → memberId={}", member.getId());

			return;
		}

		log.info("📦 진행 중인 거래 상세 개수 = {} → memberId={}", tradeDetailsList.size(), member.getId());
		LocalDateTime now = LocalDateTime.now();

		// 중복 (tradeId:wifiId) 이벤트 방지
		HashSet<String> published = new HashSet<>();

		for (TradeDetails tradeDetails : tradeDetailsList) {

			Trade trade = tradeDetails.getTrade();
			com.dapanda.product.entity.Product product = tradeDetails.getProduct();

			Wifi wifi = productRepository.findWifiByProductId(product.getId());
			log.info("🔍 상품 정보 → itemType={}, wifi null 여부={}, wifiId={}",
					product.getItemType(),
					wifi == null,
					wifi != null ? wifi.getId() : null
			);

			if (wifi == null || wifi.getStartTime() == null || wifi.getEndTime() == null) {

				continue;
			}

			log.info("🧪 필터 통과한 wifi → id={}, startTime={}, endTime={}",
					wifi.getId(), wifi.getStartTime(), wifi.getEndTime());

			// 진짜 '진행 중'인지: startTime <= now < endTime
			boolean isOngoing =
					!now.isBefore(wifi.getStartTime()) && wifi.getEndTime().isAfter(now);

			log.info("⏰ 현재 시간={}, startTime={}, endTime={}, isOngoing={}",
					now, wifi.getStartTime(), wifi.getEndTime(), isOngoing);

			if (!isOngoing) {

				continue;
			}

			String key = trade.getId() + ":" + wifi.getId();
			if (!published.add(key)) {

				continue; // 이미 보낸 조합이면 스킵
			}

			log.info("🚨 진행 중 와이파이 거래 알림 발송 → memberId={}, tradeId={}, wifiId={}",
					member.getId(), trade.getId(), wifi.getId());

			eventPublisher.publishEvent(WifiTradeEvent.createStartEvent(
					trade.getId(),
					member.getId(),
					wifi.getStartTime().toLocalTime(),
					wifi.getEndTime().toLocalTime()
			));
		}
	}
}
