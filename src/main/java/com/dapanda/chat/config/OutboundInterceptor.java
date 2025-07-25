package com.dapanda.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

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

		Map<String, Object> nativeHeaders = (Map<String, Object>) headers.get(SimpMessageHeaderAccessor.NATIVE_HEADERS);

		String senderId = getFirstNativeHeader(nativeHeaders, MessagePrinciple.SENDER_ID.getKey());
		String exceptMemberId = getFirstNativeHeader(nativeHeaders, MessagePrinciple.EXCEPT_MEMBER_ID.getKey());

		if (senderId != null && senderId.equals(exceptMemberId)) {

			return null;
		}

		return message;
	}

	private String getFirstNativeHeader(Map<String, Object> headers, String key) {

		if (headers == null) {

			return null;
		}

		Object value = headers.get(key);

		if (value instanceof Iterable<?> iterable) {

			for (Object v : iterable) {

				return v != null ? v.toString() : null;
			}
		}
		if (value != null) {

			return value.toString();
		}

		return null;
	}
}
