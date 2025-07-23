package com.dapanda.common.config;

import com.amazonaws.services.s3.AmazonS3;
import com.dapanda.common.controller.S3Controller;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestS3Config {

	@Bean
	public AmazonS3 amazonS3() {
		return Mockito.mock(AmazonS3.class);
	}

	@Bean
	public S3Controller s3Controller(AmazonS3 amazonS3) {
		return new S3Controller(amazonS3);
	}
}
