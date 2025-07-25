package com.dapanda;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class RedisTestContainerConfig {

	private static final String REDIS_DOCKER_IMAGE = "redis:latest";
	private static final GenericContainer<?> REDIS_CONTAINER;

	static {
		REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse(REDIS_DOCKER_IMAGE))
				.withExposedPorts(6379)
				.withReuse(true);
		REDIS_CONTAINER.start();

		System.setProperty("spring.data.redis.host", REDIS_CONTAINER.getHost());
		System.setProperty("spring.data.redis.port",
				REDIS_CONTAINER.getMappedPort(6379).toString());
	}
}
