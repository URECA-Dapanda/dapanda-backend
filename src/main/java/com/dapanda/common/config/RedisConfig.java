package com.dapanda.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.*;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	@Bean
	@Primary
	public RedisTemplate<String, String> stringRedisTemplate(
			RedisConnectionFactory connectionFactory) {

		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());

		return template;
	}

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
