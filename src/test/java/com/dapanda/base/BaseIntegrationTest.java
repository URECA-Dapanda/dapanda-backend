package com.dapanda.base;

import com.dapanda.RedisTestContainerConfig;
import com.dapanda.TestConfig;
import com.dapanda.common.config.TestS3Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class, RedisTestContainerConfig.class, TestS3Config.class})
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
public abstract class BaseIntegrationTest {

	@Autowired
	protected EntityManager entityManager;

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("chatPubSub")
	protected RedisTemplate<String, String> redisTemplate;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected WebApplicationContext context;

	protected MockMvc mockMvc;

	@BeforeEach
	void commonSetUp(RestDocumentationContextProvider restDocumentationContextProvider) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentationContextProvider);

		cleanupDatabase();
		cleanupRedis();
	}

	/**
	 * 데이터베이스 초기화 - 외래키 순서를 고려한 DELETE 방식
	 * TRUNCATE보다 안전하고 외래키 제약조건 문제 없음
	 */
	private void cleanupDatabase() {

		entityManager.clear();

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

		// 외래키 의존성 순서대로 삭제 (자식 테이블부터)
		jdbcTemplate.execute("DELETE FROM chat_message_read_status");
		jdbcTemplate.execute("DELETE FROM chat_message");
		jdbcTemplate.execute("DELETE FROM chat_participant");
		jdbcTemplate.execute("DELETE FROM chat_room");
		jdbcTemplate.execute("DELETE FROM review");
		jdbcTemplate.execute("DELETE FROM trade_details");
		jdbcTemplate.execute("DELETE FROM trade");
		jdbcTemplate.execute("DELETE FROM report");
		jdbcTemplate.execute("DELETE FROM product_image");
		jdbcTemplate.execute("DELETE FROM wifi");
		jdbcTemplate.execute("DELETE FROM mobile_data");
		jdbcTemplate.execute("DELETE FROM product");
		jdbcTemplate.execute("DELETE FROM plan");
		jdbcTemplate.execute("DELETE FROM refresh_token");
		jdbcTemplate.execute("DELETE FROM fcm_token");
		jdbcTemplate.execute("DELETE FROM notification_entity");
		jdbcTemplate.execute("DELETE FROM payment");
		jdbcTemplate.execute("DELETE FROM member");

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
	}

	protected void cleanupRedis() {

		if (redisTemplate != null) {
			try {
				System.out.println("🧹 Cleaning Redis...");

				// 현재 Redis에 저장된 모든 키 조회
				Set<String> keys = redisTemplate.keys("*");

				if (keys != null && !keys.isEmpty()) {
					// 모든 키 삭제
					Long deletedCount = redisTemplate.delete(keys);
					System.out.println("✅ Redis cleaned: " + deletedCount + " keys deleted");
				} else {
					System.out.println("✅ Redis already clean");
				}

			} catch (Exception e) {
				System.out.println("⚠️ Failed to clean Redis: " + e.getMessage());
			}
		} else {
			System.out.println("ℹ️ Redis not available for cleanup");
		}
	}
}
