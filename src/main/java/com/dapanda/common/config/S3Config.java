package com.dapanda.common.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Profile("!test")
@Configuration
public class S3Config {

	@Value("${AWS_ACCESS_KEY}")
	private String accessKey;

	@Value("${AWS_SECRET_KEY}")
	private String secretKey;

	@Value("${AWS_REGION}")
	private String region;

	@Bean
	public AmazonS3 amazonS3() {

		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

		return AmazonS3ClientBuilder.standard()
				.withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.build();
	}
}
