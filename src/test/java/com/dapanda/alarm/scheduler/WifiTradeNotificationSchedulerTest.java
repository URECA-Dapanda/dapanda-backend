package com.dapanda.alarm.scheduler;

import com.dapanda.alarm.event.WifiTradeEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WifiTradeNotificationSchedulerTest {

	@Mock
	ApplicationEventPublisher eventPublisher;

	WifiTradeNotificationScheduler scheduler;

	@BeforeEach
	void setUp() {
		scheduler = new WifiTradeNotificationScheduler(eventPublisher);
	}

	@Test
	@DisplayName("와이파이 상품 구매 시 알림 이벤트 발행을 성공한다")
	void successScheduleNotificationPublishesEventAfterDelay() throws InterruptedException {

		// given
		Long memberId = 1L;
		Long tradeId = 100L;
		LocalTime startTime = LocalTime.now().plusSeconds(1);
		LocalTime endTime = startTime.plusMinutes(30);

		// when
		scheduler.scheduleNotification(memberId, tradeId, startTime, endTime);

		// then: delay 이후에 이벤트가 발행됐는지 확인
		Thread.sleep(1500); // 1.5초 기다림
		verify(eventPublisher, timeout(2000).times(1))
				.publishEvent(any(WifiTradeEvent.class));
	}
}
