package com.dapanda.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 로그, 디버깅 목적 클래스
 */
@Slf4j
@Component
public class WebSocketEventListener {

	private final Set<String> sessions = ConcurrentHashMap.newKeySet();

	@EventListener
	public void connectHandle(SessionConnectEvent event) {

		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		sessions.add(accessor.getSessionId());

		createLog(accessor);
	}

	@EventListener
	public void disconnectHandle(SessionDisconnectEvent event) {

		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		sessions.remove(accessor.getSessionId());

		createLog(accessor);
	}

	private void createLog(StompHeaderAccessor accessor) {

		log.info("[WebSocketEventListener] Disconnect Session ID = {}", accessor.getSessionId());
		log.info("[WebSocketEventListener] Total Session = {}", sessions.size());
	}
}
