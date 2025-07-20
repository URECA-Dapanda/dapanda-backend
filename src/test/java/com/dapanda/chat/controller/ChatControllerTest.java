package com.dapanda.chat.controller;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.chat.entity.ChatParticipant;
import com.dapanda.chat.entity.ChatRoom;
import com.dapanda.chat.repository.ChatParticipantRepository;
import com.dapanda.chat.repository.ChatRoomRepository;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.Product;
import com.dapanda.product.entity.ProductFixture;
import com.dapanda.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("채팅 컨트롤러 테스트")
class ChatControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	private MockMvc mockMvc;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ChatRoomRepository chatRoomRepository;
	@Autowired
	private ChatParticipantRepository chatParticipantRepository;

	@BeforeEach
	void restDocsSetUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = TestConfig.createMockMvc(context, restDocumentation);

		cleanupDatabase();
	}

	private void cleanupDatabase() {

		entityManager.clear();

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

		jdbcTemplate.execute("TRUNCATE TABLE chat_room");
		jdbcTemplate.execute("TRUNCATE TABLE chat_participant");
		jdbcTemplate.execute("TRUNCATE TABLE product");
		jdbcTemplate.execute("TRUNCATE TABLE member");

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
	}

	@Nested
	@DisplayName("채팅방 생성 API")
	class CreateChatRoom {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("생성된 채팅방 아이디를 반환한다")
			public void createChatRoomTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());

				Product product = productRepository.save(ProductFixture.createProduct1(seller));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(buyer.getId());

				//when & then
				mockMvc.perform(post("/api/products/{productId}/chat-room", product.getId())
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.chatRoomId").exists())
						.andDo(document("chat/create-chat-room",
										pathParameters(
												parameterWithName("productId").description("채팅방을 생성할 상품의 아이디 (필수)")
										),
										responseFields(
												fieldWithPath("code").description("상태 코드"),
												fieldWithPath("message").description("처리 결과 메시지"),
												fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
												fieldWithPath("data.chatRoomId").description("생성된 채팅방 아이디")
										)
								)
						);
			}

			@Test
			@DisplayName("기존에 생성된 채팅방이 있다면 기존 채팅방 아이디를 반환한다")
			public void returnPreviousChatRoomTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());

				Product product = productRepository.save(ProductFixture.createProduct1(seller));

				ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.of(product));
				chatParticipantRepository.save(ChatParticipant.of(chatRoom, seller));
				chatParticipantRepository.save(ChatParticipant.of(chatRoom, buyer));

				CustomUserDetails userDetails = mock(CustomUserDetails.class);

				given(userDetails.getId()).willReturn(buyer.getId());

				//when & then
				mockMvc.perform(post("/api/products/{productId}/chat-room", product.getId())
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, Collections.emptyList()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.chatRoomId").value(chatRoom.getId()))
						.andDo(document("chat/chat-room-already-exist",
										pathParameters(
												parameterWithName("productId").description("채팅방을 생성할 상품의 아이디 (필수)")
										),
										responseFields(
												fieldWithPath("code").description("상태 코드"),
												fieldWithPath("message").description("처리 결과 메시지"),
												fieldWithPath("data").description("응답 데이터 (에러시 반환되지 않음)"),
												fieldWithPath("data.chatRoomId").description("기존 채팅방 아이디")
										)
								)
						);
			}
		}
	}
}
