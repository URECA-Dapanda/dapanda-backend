package com.dapanda.alarm.service;

import com.dapanda.alarm.event.WifiTradeStartEvent;
import com.dapanda.member.entity.Member;
import com.dapanda.product.entity.ItemType;
import com.dapanda.product.entity.Product;
import com.dapanda.product.entity.Wifi;
import com.dapanda.product.repository.WifiRepository;
import com.dapanda.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WifiTradeNotifier {

    private final TradeRepository tradeRepository;
    private final WifiRepository wifiRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void notifyOngoingTradeIfExists(Member member) {
        log.info("🛎️ [알림] notifyOngoingTradeIfExists 호출됨 → memberId={}", member.getId());

        tradeRepository.findOngoingWifiTradeByMemberId(member.getId())
                .ifPresentOrElse(trade -> {
                    log.info("📦 진행 중인 거래 발견 → tradeId={}, tradeDetails 수={}", trade.getId(), trade.getTradeDetails().size());

                    trade.getTradeDetails().forEach(td -> {
                        Product product = td.getProduct();
                        Wifi wifi = product.getWifiFromRepository(wifiRepository);
                        log.info("🔍 상품 정보 → itemType={}, wifi null 여부={}, wifiId={}",
                                product.getItemType(),
                                wifi == null,
                                wifi != null ? wifi.getId() : null
                        );
                    });

                    trade.getTradeDetails().stream()
                            .map(td -> td.getProduct())
                            .map(p -> p.getWifiFromRepository(wifiRepository))
                            .filter(wifi -> wifi != null)
                            .peek(wifi -> log.info("🧪 필터 통과한 wifi → id={}, startTime={}, endTime={}", wifi.getId(), wifi.getStartTime(), wifi.getEndTime()))
                            .filter(wifi -> {
                                boolean isOngoing = wifi.getEndTime().isAfter(LocalDateTime.now());
                                log.info("⏰ 현재 시간={}, endTime={}, isOngoing={}", LocalDateTime.now(), wifi.getEndTime(), isOngoing);
                                return isOngoing;
                            })
                            .findFirst()
                            .ifPresent(wifi -> {
                                log.info("🚨 로그인 후 진행 중 거래 알림 발송 → memberId={}, tradeId={}", member.getId(), trade.getId());
                                eventPublisher.publishEvent(WifiTradeStartEvent.of(
                                        trade.getId(),
                                        member.getId(),
                                        wifi.getStartTime().toLocalTime(),
                                        wifi.getEndTime().toLocalTime()
                                ));
                            });
                }, () -> {
                    log.info("❌ 진행 중인 와이파이 거래 없음 → memberId={}", member.getId());
                });
    }
}

