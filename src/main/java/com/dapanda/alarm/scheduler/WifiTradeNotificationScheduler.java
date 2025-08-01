package com.dapanda.alarm.scheduler;

import com.dapanda.alarm.event.WifiTradeStartEvent;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WifiTradeNotificationScheduler {

	private final ApplicationEventPublisher eventPublisher;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
			5); // 병렬 예약 지원

	public void scheduleNotification(Long memberId, Long tradeId, LocalTime startTime,
			LocalTime endTime) {

		long delay = Duration.between(LocalTime.now(), startTime).toMillis();

		if (delay < 0) {
			log.warn("예약 시간이 과거입니다. 즉시 이벤트를 발행합니다.");
			publishNow(memberId, tradeId, startTime, endTime);

			return;
		}

		scheduler.schedule(() -> publishNow(memberId, tradeId, startTime, endTime), delay,
				TimeUnit.MILLISECONDS);
	}

	private void publishNow(Long memberId, Long tradeId, LocalTime startTime, LocalTime endTime) {

		log.info("예약된 시간 도달, 알림 발행 -> memberId: {}, tradeId: {}", memberId, tradeId);
		eventPublisher.publishEvent(WifiTradeStartEvent.of(memberId, tradeId, startTime, endTime));
	}
}
