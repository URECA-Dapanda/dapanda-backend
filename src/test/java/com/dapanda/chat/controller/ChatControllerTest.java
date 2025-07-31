package com.dapanda.chat.controller;

import com.dapanda.TestConfig;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.chat.entity.*;
import com.dapanda.chat.repository.*;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Comparator;
import java.util.List;

import static com.dapanda.TestConstants.Pagination.CHAT_MESSAGE_HISTORY_DEFAULT_SIZE;
import static com.dapanda.TestConstants.Pagination.DEFAULT_SIZE_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
	@Autowired
	private MobileDataRepository mobileDataRepository;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	@Autowired
	private WifiRepository wifiRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ChatMessageReadStatusRepository chatMessageReadStatusRepository;

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
		jdbcTemplate.execute("TRUNCATE TABLE chat_message");
		jdbcTemplate.execute("TRUNCATE TABLE chat_message_read_status");
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

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				//when & then
				mockMvc.perform(post("/api/products/{productId}/chat-room", product.getId())
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
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

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				//when & then
				mockMvc.perform(post("/api/products/{productId}/chat-room", product.getId())
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
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

	@Nested
	@DisplayName("참여중인 채팅방 조회 API")
	class ReadChatRoom {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("페이징 조건이 없으면 기본 값으로 조회된다")
			public void readChatRoomDefaultPagingTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				List<MobileData> mobileDataList = mobileDataRepository.saveAll(MobileDataFixture.createMobileDataList());

				List<Product> productList = productRepository.saveAll(ProductFixture.createProductList(seller, mobileDataList, ProductState.ACTIVE));

				List<ChatRoom> chatRoomList = chatRoomRepository.saveAll(ChatRoomFixture.createChatRoomList(productList));

				chatParticipantRepository.saveAll(ChatParticipantFixture.createChatParticipantList(chatRoomList, buyer, seller));

				//when & then
				mockMvc.perform(get("/api/chat-room")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").exists())
						.andExpect(jsonPath("$.data.data.length()").value(Math.min(DEFAULT_SIZE_2, chatRoomList.size())))
						.andExpect(jsonPath("$.data.pageInfo").exists())
						.andExpect(jsonPath("$.data.pageInfo.hasNext").isBoolean())
						.andExpect(jsonPath("$.data.pageInfo.size").isNumber())
						.andExpect(jsonPath("$.data.pageInfo.nextCursorId").isNumber())
						.andDo(document("chat/read-chat-room",
								queryParameters(
										parameterWithName("cursorId").description("커서 아이디 (선택)").optional(),
										parameterWithName("size").description("페이지 크기 (선택, 기본값 = 2, 최대 = 100)").optional(),
										parameterWithName("chatRoomReadOption").description("채팅방 조회 옵션 (선택, 기본값 = ALL: 전체, BUYER: 구매자 기준, SELLER: 판매자 기준)").optional()
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										fieldWithPath("data").description("페이징 처리된 리뷰 데이터"),
										// data.data[] 배열 내의 각 리뷰 객체 필드
										fieldWithPath("data.data[]").description("조회된 채팅방 목록"),
										fieldWithPath("data.data[].chatRoomId").description("채팅방 아이디"),
										fieldWithPath("data.data[].createdAt").description("채팅방 생성 시간"),
										fieldWithPath("data.data[].lastMessageAt").description("마지막 메시지 시간"),
										fieldWithPath("data.data[].lastMessage").description("마지막 메시지"),
										fieldWithPath("data.data[].unreadCount").description("읽지 않은 메시지 수"),
										fieldWithPath("data.data[].senderId").description("채팅 상대 회원 아이디"),
										fieldWithPath("data.data[].senderName").description("채팅 상대 회원 이름"),
										fieldWithPath("data.data[].senderProfileImageUrl").description("채팅 상대 이미지 URL"),
										fieldWithPath("data.data[].productId").description("채팅방의 상품 아이디"),
										fieldWithPath("data.data[].itemId").description("상품의 상세 아이디"),
										fieldWithPath("data.data[].itemType").description("상품의 상세 타입"),
										fieldWithPath("data.data[].dataAmount").description("모바일 데이터 량").type(JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.data[].remainAmount").description("모바일 데이터 잔량").type(JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.data[].startTime").description("와이파이 이용 시작 시간").type(JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.data[].endTime").description("와이파이 이용 종료 시간").type(JsonFieldType.NUMBER).optional(),
										fieldWithPath("data.pageInfo").description("페이지 정보"),
										fieldWithPath("data.pageInfo.size").description("현재 페이지 크기"),
										fieldWithPath("data.pageInfo.hasNext").description("다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.nextCursorId").description("다음 페이지 조회 시 사용할 커서 아이디 (다음 페이지가 없으면 null)")
								)
						));
			}

			@Test
			@DisplayName("조회 결과가 없으면 빈 리스트를 반환한다")
			public void readChatRoomEmptyListTest() throws Exception {

				//given
				Member buyer = memberRepository.save(MemberFixture.createMember2());

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				//when & then
				mockMvc.perform(get("/api/chat-room")
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").exists())
						.andExpect(jsonPath("$.data.data.length()").value(0))
						.andExpect(jsonPath("$.data.pageInfo").exists())
						.andExpect(jsonPath("$.data.pageInfo.hasNext").value(false))
						.andExpect(jsonPath("$.data.pageInfo.size").value(DEFAULT_SIZE_2))
						.andExpect(jsonPath("$.data.pageInfo.nextCursorId").doesNotExist())
						.andDo(document("chat/read-chat-room-empty-list",
								queryParameters(
										parameterWithName("cursorId").description("커서 아이디 (선택)").optional(),
										parameterWithName("size").description("페이지 크기 (선택, 기본값 = 2, 최대 = 100)").optional(),
										parameterWithName("chatRoomReadOption").description("채팅방 조회 옵션 (선택, 기본값 = ALL: 전체, BUYER: 구매자 기준, SELLER: 판매자 기준)").optional()
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										fieldWithPath("data").description("페이징 처리된 리뷰 데이터"),
										// data.data[] 배열 내의 각 리뷰 객체 필드
										fieldWithPath("data.data[]").description("조회된 채팅방 목록"),
										fieldWithPath("data.pageInfo").description("페이지 정보"),
										fieldWithPath("data.pageInfo.size").description("현재 페이지 크기"),
										fieldWithPath("data.pageInfo.hasNext").description("다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.nextCursorId").description("다음 페이지 조회 시 사용할 커서 아이디 (다음 페이지가 없으면 null)")
								)
						));
			}
		}
	}

	@Nested
	@DisplayName("참여중인 채팅방의 채팅 이력 조회 API")
	class ReadChatMessageHistory {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("페이징 조건이 없으면 기본 값으로 조회된다")
			public void readChatMessageHistoryDefaultPagingTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				Wifi wifi = wifiRepository.save(WifiFixture.createWifi());

				Product product = productRepository.save(ProductFixture.createWifiProduct(seller, wifi));

				ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.createChatRoom(product));

				chatParticipantRepository.saveAll(ChatParticipantFixture.createChatParticipant(chatRoom, buyer, seller));

				List<ChatMessage> chatMessageList = ChatMessageFixture.createChatMessageList(chatRoom, buyer, seller);

				List<ChatMessage> chatMessages = chatMessageRepository.saveAll(chatMessageList);

				List<Integer> expectedSortedIds = chatMessages.stream()
						.map(ChatMessage::getId)
						.sorted(Comparator.reverseOrder()) // ID 오름차순으로 정렬
						.limit(CHAT_MESSAGE_HISTORY_DEFAULT_SIZE) // 기본 페이지 크기만큼만 가져옴
						.map(Long::intValue)
						.toList();

				//when & then
				mockMvc.perform(get("/api/chat-room/{chatRoomId}/history", chatRoom.getId())
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").exists())
						.andExpect(jsonPath("$.data.data.length()").value(Math.min(CHAT_MESSAGE_HISTORY_DEFAULT_SIZE, chatMessages.size())))
						.andExpect(jsonPath("$.data.pageInfo").exists())
						.andExpect(jsonPath("$.data.pageInfo.hasNext").isBoolean())
						.andExpect(jsonPath("$.data.pageInfo.size").isNumber())
						.andExpect(jsonPath("$.data.pageInfo.nextCursorId").isNumber())
						.andExpect(jsonPath("$.data.data[*].chatMessageId", Matchers.contains(expectedSortedIds.toArray(new Integer[0]))))
						.andExpect(jsonPath("$.data.memberId").isNumber())
						.andDo(document("chat/read-chat-message-history",
								pathParameters(
										parameterWithName("chatRoomId").description("채팅 이력을 조회할 채팅방 아이디 (필수)")
								),
								queryParameters(
										parameterWithName("cursorId").description("커서 아이디 (선택)").optional(),
										parameterWithName("size").description("페이지 크기 (선택, 기본값 = 20, 최대 = 100)").optional()
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										fieldWithPath("data").description("페이징 처리된 리뷰 데이터"),
										// data.data[] 배열 내의 각 리뷰 객체 필드
										fieldWithPath("data.data[]").description("조회된 채팅방 목록"),
										fieldWithPath("data.data[].chatMessageId").description("메시지 아이디"),
										fieldWithPath("data.data[].message").description("메시지"),
										fieldWithPath("data.data[].createdAt").description("메시지 생성 시간"),
										fieldWithPath("data.data[].isMine").description("조회 요청한 사용자 소유 여부"),
										fieldWithPath("data.pageInfo").description("페이지 정보"),
										fieldWithPath("data.pageInfo.size").description("현재 페이지 크기"),
										fieldWithPath("data.pageInfo.hasNext").description("다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.nextCursorId").description("다음 페이지 조회 시 사용할 커서 아이디 (다음 페이지가 없으면 null)"),
										fieldWithPath("data.memberId").description("조회하는 회원의 아이디")
								)
						));
			}

			@Test
			@DisplayName("조회 결과가 없으면 빈 리스트를 반환한다")
			public void readChatMessageEmptyListTest() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				Wifi wifi = wifiRepository.save(WifiFixture.createWifi());

				Product product = productRepository.save(ProductFixture.createWifiProduct(seller, wifi));

				ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.createChatRoom(product));

				chatParticipantRepository.saveAll(ChatParticipantFixture.createChatParticipant(chatRoom, buyer, seller));

				//when & then
				mockMvc.perform(get("/api/chat-room/{chatRoomId}/history", chatRoom.getId())
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andExpect(jsonPath("$.data.data").exists())
						.andExpect(jsonPath("$.data.data.length()").value(0))
						.andExpect(jsonPath("$.data.pageInfo").exists())
						.andExpect(jsonPath("$.data.pageInfo.hasNext").value(false))
						.andExpect(jsonPath("$.data.pageInfo.size").value(CHAT_MESSAGE_HISTORY_DEFAULT_SIZE))
						.andExpect(jsonPath("$.data.pageInfo.nextCursorId").doesNotExist())
						.andExpect(jsonPath("$.data.memberId").isNumber())
						.andDo(document("chat/read-chat-message-history-empty-list",
								pathParameters(
										parameterWithName("chatRoomId").description("채팅 이력을 조회할 채팅방 아이디 (필수)")
								),
								queryParameters(
										parameterWithName("cursorId").description("커서 아이디 (선택)").optional(),
										parameterWithName("size").description("페이지 크기 (선택, 기본값 = 20, 최대 = 100)").optional()
								),
								responseFields(
										fieldWithPath("code").description("응답 코드"),
										fieldWithPath("message").description("응답 메시지"),
										// data.data[] 배열 내의 각 리뷰 객체 필드
										fieldWithPath("data.data[]").description("조회된 채팅방 목록"),
										fieldWithPath("data.pageInfo").description("페이지 정보"),
										fieldWithPath("data.pageInfo.size").description("현재 페이지 크기"),
										fieldWithPath("data.pageInfo.hasNext").description("다음 페이지 존재 여부"),
										fieldWithPath("data.pageInfo.nextCursorId").description("다음 페이지 조회 시 사용할 커서 아이디 (다음 페이지가 없으면 null)"),
										fieldWithPath("data.memberId").description("조회하는 회원의 아이디")
								)
						));
			}
		}
	}

	@Nested
	@DisplayName("메시지 읽음 상태 업데이트 API")
	class UpdateChatMessageReadStatus {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("마지막으로 읽은 메시지 아이디를 저장한다")
			public void updateLastMessageId() throws Exception {

				//given
				Member seller = memberRepository.save(MemberFixture.createMember1());
				Member buyer = memberRepository.save(MemberFixture.createMember2());

				CustomUserDetails userDetails = CustomUserDetails.from(buyer);

				Wifi wifi = wifiRepository.save(WifiFixture.createWifi());

				Product product = productRepository.save(ProductFixture.createWifiProduct(seller, wifi));

				ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.createChatRoom(product));

				chatParticipantRepository.saveAll(ChatParticipantFixture.createChatParticipant(chatRoom, buyer, seller));

				List<ChatMessage> chatMessageList = chatMessageRepository.saveAll(
						ChatMessageFixture.createChatMessageList(chatRoom, buyer, seller)
				);

				ChatMessage lastChatMessage = chatMessageList.get(chatMessageList.size() - 1);

				System.out.println("lastChatMessage = " + lastChatMessage.getMember());

				//when & then
				mockMvc.perform(post("/api/chat-messages/{chatMessageId}/read-status", lastChatMessage.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.with(authentication(new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities()
								))))
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
						.andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
						.andDo(document("chat/update-chat-message-read-status",
								pathParameters(
										parameterWithName("chatMessageId").description("마지막으로 읽은 메시지 아이디")
								),
								responseFields(
										fieldWithPath("code").description("상태 코드"),
										fieldWithPath("message").description("처리 결과 메시지")
								)
						));

				ChatMessageReadStatus savedReadStatus = chatMessageReadStatusRepository
						.findFirstByChatRoomAndMember(chatRoom, buyer)
						.orElseThrow();

				assertThat(savedReadStatus.getChatMessage().getId()).isEqualTo(lastChatMessage.getId());
				assertThat(savedReadStatus.getChatRoom().getId()).isEqualTo(chatRoom.getId());
				assertThat(savedReadStatus.getMember().getId()).isEqualTo(buyer.getId());
			}
		}
	}
}
