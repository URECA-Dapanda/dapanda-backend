package com.dapanda.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketHandler implements ChannelInterceptor {

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		log.info("preSend 로그");

		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && accessor.getCommand() != null) {

			log.info("accessor 에 인증 객체 등록");
			accessor.setUser(authentication);
		}

		return message;
	}
}
