package com.dapanda.alarm.handler;

import com.dapanda.alarm.dto.AlarmMessage;
import com.dapanda.alarm.event.WifiTradeStartEvent;
import com.dapanda.fcmToken.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WifiTradeStartEventHandler {

	private final SimpMessagingTemplate messagingTemplate;
	private final FcmTokenService fcmTokenService;

	@Async
	@EventListener
	public void handleWifiTradeStart(WifiTradeStartEvent event) {
		log.info("WIFI 사용 시작 이벤트 tradeId : {}, memberId : {}", event.getTradeId(),
				event.getMemberId());

		AlarmMessage message = new AlarmMessage(
				event.getTradeId(),
				event.getStartTime().toString(), // 예: "14:00"
				event.getEndTime().toString()
		);

		messagingTemplate.convertAndSend(
				"/sub/alarm" + event.getMemberId(), message
		);

		fcmTokenService.notifyWifiStart(event.getMemberId());
	}
}
