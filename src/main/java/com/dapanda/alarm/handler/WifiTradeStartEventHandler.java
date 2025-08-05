package com.dapanda.alarm.handler;

import com.dapanda.alarm.event.WifiTradeEvent;
import com.dapanda.chat.config.WebSocketPath;
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
	public void handleWifiTradeStart(WifiTradeEvent event) {
		log.info("WIFI 사용 시작 이벤트 tradeId : {}, memberId : {}", event.tradeId(),
				event.memberId());

		messagingTemplate.convertAndSend(
				WebSocketPath.SUB.getPath() + "/" +
						WebSocketPath.ALARM.getPath() + "/" +
						event.memberId(),
				event
		);

		fcmTokenService.notifyWifiStart(event.memberId());
	}
}
