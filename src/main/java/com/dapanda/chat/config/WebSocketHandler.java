package com.dapanda.chat.config;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class WebSocketHandler implements ChannelInterceptor {

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		log.info("preSend 로그");

		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		log.info("accessor.getUser = {}", accessor.getUser());
		log.info("accessor.getSessionAttributes = {}", accessor.getSessionAttributes());
		log.info("accessor : {}", accessor);

		if (accessor.getUser() == null && accessor.getSessionAttributes() != null) {

			Authentication authentication = (Authentication) accessor.getSessionAttributes().get("SPRING_SECURITY_PRINCIPAL");

			log.info("authentication = {}", authentication);

			if (authentication != null) {
				accessor.setUser(authentication);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}


		Authentication authentication = (Authentication) accessor.getUser();

		CustomUserDetails userDetails = null;

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {

			userDetails = (CustomUserDetails) authentication.getPrincipal();
		}

		if (userDetails == null) {

			log.warn("[WebSocketHandler] userDetails == null");

			throw new GlobalException(ResultCode.STOMP_UNAUTHORIZED);

		} else {

			log.debug("[WebSocketHandler] memberId = {}, memberName = {}", userDetails.getId(), userDetails.getUsername());
		}

		switch (Objects.requireNonNull(accessor.getCommand())) {
			case CONNECT -> {
				log.info("STOMP CONNECT");

				log.info("User {} ({}) connected.", userDetails.getUsername(), userDetails.getId());

				log.debug("Connect Headers: {}", accessor.getMessageHeaders());
			}
			case SUBSCRIBE -> {
				String subscribeDestination = accessor.getDestination();

				log.info("STOMP SUBSCRIBE received. Destination: {}", subscribeDestination);

				log.info("User {} ({}) attempting to SUBSCRIBE to {}", userDetails.getUsername(), userDetails.getId(), subscribeDestination);

				//TODO SUBSCRIBE 권한 검증 로직 추가
			}
			case DISCONNECT -> {

				log.info("STOMP DISCONNECT received. Session ID: {}", accessor.getSessionId());

				log.info("User {} ({}) disconnected.", userDetails.getUsername(), userDetails.getId());
			}
		}

		return message;
	}
}
