package com.dapanda.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.*;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

//	@Bean
//	@Qualifier("chatPubSub")
//	public RedisConnectionFactory chatPubSubFactor() {
//
//		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//
//		configuration.setHostName(host);
//		configuration.setPort(port);
//
//		return new LettuceConnectionFactory(configuration);
//	}
//
//	@Bean
//	@Qualifier("chatPubSub")
//	public StringRedisTemplate stringRedisTemplate(
//			@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory) {
//
//		return new StringRedisTemplate(redisConnectionFactory);
//	}
//
//	@Bean
//	public RedisMessageListenerContainer redisMessageListenerContainer(
//			@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
//			MessageListenerAdapter messageListenerAdapter) {
//
//		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//
//		container.setConnectionFactory(redisConnectionFactory);
//		container.addMessageListener(messageListenerAdapter, new PatternTopic("chat"));
//
//		return container;
//	}
//
//	@Bean
//	public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService) {
//
//		// RedisPubSubService 의 특정 메서드가 수신된 메시지를 처리할수 있도록 지정
//		return new MessageListenerAdapter(redisPubSubService, "onMessage");
//	}

	@Bean
	public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.activateDefaultTyping(
				LaissezFaireSubTypeValidator.instance,
				DefaultTyping.EVERYTHING,
				JsonTypeInfo.As.PROPERTY);

		return new GenericJackson2JsonRedisSerializer(objectMapper);
	}

	@Bean
	public RedisCacheConfiguration redisCacheConfiguration(
			GenericJackson2JsonRedisSerializer jsonRedisSerializer) {

		return RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
						new StringRedisSerializer()))
				.serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(
								jsonRedisSerializer));
	}
}
