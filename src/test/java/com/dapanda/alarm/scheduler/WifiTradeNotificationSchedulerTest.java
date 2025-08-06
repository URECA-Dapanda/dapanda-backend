package com.dapanda.alarm.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.dapanda.alarm.event.WifiTradeEvent;
import com.dapanda.trade.repository.TradeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("거래 알림 스케줄러 테스트")
class WifiTradeNotificationSchedulerTest {

	@Mock
	ApplicationEventPublisher eventPublisher;
	@Mock
	TradeRepository tradeRepository;

	WifiTradeNotificationScheduler scheduler;

	@BeforeEach
	void setUp() {
		scheduler = new WifiTradeNotificationScheduler(eventPublisher, tradeRepository);
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
		when(tradeRepository.findTradeDateById(tradeId))
				.thenReturn(LocalDate.now());
		scheduler.scheduleNotification(tradeId, memberId, startTime, endTime);

		// then: delay 이후에 이벤트가 발행됐는지 확인
		Thread.sleep(1500); // 1.5초 기다림
		verify(eventPublisher, timeout(2000).times(1))
				.publishEvent(any(WifiTradeEvent.class));
	}
}
