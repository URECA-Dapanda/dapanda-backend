package com.dapanda.chat.config;

import com.dapanda.common.config.AllowedOriginPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {

		registry.addEndpoint(WebSocketPath.CONN.getPath())
				.setAllowedOrigins(AllowedOriginPath.LOCAL.getPath(), AllowedOriginPath.PROD.getPath());
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {

		log.info("메시지 브로커 설정 - PUB: {}, SUB: {}",
				WebSocketPath.PUB.getPath(), WebSocketPath.SUB.getPath());

		// @MessageMapping 메서드로 라우팅하기 위한 url 패턴 지정
		registry.setApplicationDestinationPrefixes(WebSocketPath.PUB.getPath());

		// 메시지를 수신(sub, 구독)하기 위한 url 패턴 지정
		registry.enableSimpleBroker(WebSocketPath.SUB.getPath());
	}
}
