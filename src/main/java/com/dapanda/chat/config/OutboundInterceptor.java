package com.dapanda.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundInterceptor implements ChannelInterceptor {

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		MessageHeaders headers = message.getHeaders();

		if (!SimpMessageType.MESSAGE.equals(headers.get(MessagePrinciple.SIMP_MESSAGE_TYPE.getKey()))) {

			return message;
		}

		Object nativeHeaderObj = headers.get(SimpMessageHeaderAccessor.NATIVE_HEADERS);
		if (!(nativeHeaderObj instanceof Map)) {
			return message;
		}

		Object messageSenderSession = ((Map<String, List<String>>) headers.get(SimpMessageHeaderAccessor.NATIVE_HEADERS)).get(MessagePrinciple.SIMP_SESSION_ID.getKey()).get(0);
		Object messageReceiverSession = headers.get(MessagePrinciple.SIMP_SESSION_ID.getKey());

		if (messageSenderSession.equals(messageReceiverSession)) {

			return null;
		}

		return message;
	}
}
