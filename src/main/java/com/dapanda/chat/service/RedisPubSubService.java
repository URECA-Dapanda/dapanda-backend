package com.dapanda.chat.service;

import com.dapanda.chat.config.WebSocketPath;
import com.dapanda.chat.dto.response.CreateMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPubSubService implements MessageListener {

	private final StringRedisTemplate stringRedisTemplate;
	private final SimpMessagingTemplate messageTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(Message message, byte[] pattern) {

		String payload = new String(message.getBody());

		CreateMessageResponse response;

		try {
			response = objectMapper.readValue(payload, CreateMessageResponse.class);

			messageTemplate.convertAndSend(
					WebSocketPath.SUB.getPath() + WebSocketPath.SLASH.getPath() + response.getChatRoomId(),
					response);
		} catch (JsonProcessingException e) {

			throw new RuntimeException(e);
		}
	}

	public void publish(String channel, String message) {

		stringRedisTemplate.convertAndSend(channel, message);
	}
}
