package com.dapanda.base;

import com.dapanda.RedisTestContainerConfig;
import com.dapanda.TestConfig;
import com.dapanda.common.config.TestS3Config;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class, RedisTestContainerConfig.class, TestS3Config.class})
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
public abstract class BaseIntegrationTest {

}
