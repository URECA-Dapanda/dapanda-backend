package com.dapanda.alarm.scheduler;

import com.dapanda.alarm.event.WifiTradeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WifiTradeNotificationScheduler {

	private final ApplicationEventPublisher eventPublisher;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

	public void scheduleNotification(Long tradeId, Long memberId, LocalTime startTime,
			LocalTime endTime) {

		long startDelay = Duration.between(LocalTime.now(), startTime).toMillis();
		long endDelay = Duration.between(LocalTime.now(), endTime).toMillis();

		if (startDelay < 0) {

			log.warn("시작 시간이 과거입니다. 즉시 시작 이벤트를 발행합니다.");
			publishStartNow(tradeId, memberId, startTime, endTime);
		} else {

			scheduler.schedule(() -> publishStartNow(tradeId, memberId, startTime, endTime),
					startDelay, TimeUnit.MILLISECONDS);
		}

		if (endDelay < 0) {

			log.warn("종료 시간이 과거입니다. 즉시 종료 이벤트를 발행합니다.");
			publishEndNow(tradeId, memberId, startTime, endTime);
		} else {

			scheduler.schedule(() -> publishEndNow(tradeId, memberId, startTime, endTime),
					endDelay, TimeUnit.MILLISECONDS);
		}
	}

	private void publishStartNow(Long tradeId, Long memberId, LocalTime startTime,
			LocalTime endTime) {

		log.info("와이파이 시작 알림 발행 -> memberId: {}, tradeId: {}", memberId, tradeId);
		eventPublisher.publishEvent(WifiTradeEvent.createStartEvent(tradeId, memberId, startTime, endTime));
	}

	private void publishEndNow(Long tradeId, Long memberId, LocalTime startTime,
			LocalTime endTime) {

		log.info("와이파이 종료 알림 발행 -> memberId: {}, tradeId: {}", memberId, tradeId);
		eventPublisher.publishEvent(WifiTradeEvent.createEndEvent(tradeId, memberId, startTime, endTime));
	}

}
