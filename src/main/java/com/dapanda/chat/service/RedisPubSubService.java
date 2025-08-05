package com.dapanda.chat.service;

import com.dapanda.chat.config.WebSocketPath;
import com.dapanda.chat.dto.response.CreateMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
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

			String destination = WebSocketPath.getChatRoomSubscribePath(response.getChatRoomId());

			messageTemplate.convertAndSend(destination, response);
		} catch (JsonProcessingException e) {

			log.error("Redis Pub/Sub 받는 메시지 파싱 에러");
		}
	}

	public void publish(String channel, String message) {

		stringRedisTemplate.convertAndSend(channel, message);
	}
}
