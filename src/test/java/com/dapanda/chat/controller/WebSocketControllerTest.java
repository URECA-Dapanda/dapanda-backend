package com.dapanda.chat.controller;

import com.dapanda.base.BaseIntegrationTest;
import com.dapanda.chat.config.WebSocketPath;
import com.dapanda.chat.dto.request.CreateChatMessageRequest;
import com.dapanda.chat.dto.response.CreateMessageResponse;
import com.dapanda.chat.entity.*;
import com.dapanda.chat.repository.ChatParticipantRepository;
import com.dapanda.chat.repository.ChatRoomRepository;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("채팅 시스템 테스트")
class WebSocketControllerTest extends BaseIntegrationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private ChatRoomRepository chatRoomRepository;

	@Autowired
	private ChatParticipantRepository chatParticipantRepository;

	@Autowired
	private ProductRepository productRepository;

	private StompSession buyerStompSession;
	private final CountDownLatch connectionLatch = new CountDownLatch(1);
	private final BlockingQueue<CreateMessageResponse> receivedMessages = new LinkedBlockingDeque<>();

	private Member seller;
	private Member buyer;
	private Product product;
	private ChatRoom chatRoom;
	private String buyerToken;
	private String sellerToken;

	@BeforeEach
	void setUp() throws ExecutionException, InterruptedException, TimeoutException {

		setupTestData();
		buyerStompSession = setupWebSocketClient(buyerToken);
	}

	private StompSession setupWebSocketClient(String token) throws ExecutionException, InterruptedException, TimeoutException {

		WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
		headers.add("Cookie", "accessToken=" + token);

		WebSocketStompClient stompClient = new WebSocketStompClient((new StandardWebSocketClient()));

		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		return stompClient.connectAsync(
				String.format("ws://localhost:%d%s", port, WebSocketPath.CONN.getPath()),
				headers,
				new StompHeaders(),
				new StompSessionHandlerAdapter() {

					@Override
					public void afterConnected(StompSession session, StompHeaders connectedHeaders) {

						connectionLatch.countDown();
					}
				}
		).get(10, TimeUnit.SECONDS);
	}

	private void setupTestData() {

		// 회원 생성
		seller = MemberFixture.createMember1();
		buyer = MemberFixture.createMember2();
		memberRepository.saveAll(List.of(seller, buyer));

		// 상품 생성
		Wifi wifi = WifiFixture.createWifi();
		product = ProductFixture.createWifiProduct(seller, wifi);
		productRepository.save(product);

		// 채팅방 생성
		chatRoom = ChatRoomFixture.createChatRoom(product);
		chatRoomRepository.save(chatRoom);

		// 채팅 참여자 생성
		List<ChatParticipant> chatParticipant = ChatParticipantFixture.createChatParticipant(chatRoom, buyer, seller);

		chatParticipantRepository.saveAll(chatParticipant);

		// JWT 토큰 생성
		buyerToken = jwtTokenProvider.generateAccessToken(buyer);
		sellerToken = jwtTokenProvider.generateAccessToken(seller);
	}

	@AfterEach
	void tearDown() {

		if (buyerStompSession != null && buyerStompSession.isConnected()) {

			buyerStompSession.disconnect();
		}
	}

	@Test
	@Order(1)
	@DisplayName("WebSocket 연결 테스트")
	void websocketConnectionTest() throws Exception {

		// given
		boolean connected = connectionLatch.await(15, TimeUnit.SECONDS);

		// when & then
		assertThat(connected).isTrue();
		assertThat(buyerStompSession.isConnected()).isTrue();
	}

	@Test
	@Order(2)
	@DisplayName("채팅 메시지 송신과 수신을 할 수 있다")
	void shouldSendAndReceiveChatMessage() throws Exception {

		//given
		BlockingQueue<CreateMessageResponse> testMessages = new LinkedBlockingDeque<>();

		//구독 설정
		subscribeToChat(buyerStompSession, testMessages);

		// 구독이 완전히 설정될 때까지 대기
		Thread.sleep(1000);

		//보낼 메시지 생성
		CreateChatMessageRequest request = new CreateChatMessageRequest("Hello World");

		//메시지 전송
		sendChatMessage(buyerStompSession, request);

		//when
		//메시지 수신대기
		CreateMessageResponse receivedMessage = testMessages.poll(5, TimeUnit.SECONDS);

		//then
		assertThat(receivedMessage).isNotNull();
		assertThat(receivedMessage.getMessage()).isEqualTo(request.message());
		assertThat(receivedMessage.getSenderId()).isEqualTo(buyer.getId());
		assertThat(receivedMessage.getChatRoomId()).isEqualTo(chatRoom.getId());
	}

	@Test
	@Order(3)
	@DisplayName("Pub 경로를 Sub 하고 있는 사용자들은 메시지를 받는다")
	void shouldBroadcastMessageToMultipleClients() throws Exception {

		//given
		StompSession sellerStompClient = setupWebSocketClient(sellerToken);
		BlockingQueue<CreateMessageResponse> secondClientMessages = new LinkedBlockingDeque<>();

		//첫 번째 클라이언트 구독
		subscribeToChat(buyerStompSession, receivedMessages);

		//두 번째 클라이언트 구독
		subscribeToChat(sellerStompClient, secondClientMessages);

		//구독이 완전히 설정될 때까지 대기
		Thread.sleep(1000);

		CreateChatMessageRequest request = new CreateChatMessageRequest("Broadcasting test message");

		//when
		sendChatMessage(buyerStompSession, request);

		//then
		CreateMessageResponse buyerClientMessage = receivedMessages.poll(5, TimeUnit.SECONDS);
		CreateMessageResponse sellerClientMessage = secondClientMessages.poll(5, TimeUnit.SECONDS);

		assertThat(buyerClientMessage).isNotNull();
		assertThat(sellerClientMessage).isNotNull();
		assertThat(buyerClientMessage.getMessage()).isEqualTo(sellerClientMessage.getMessage());

		//clean up
		sellerStompClient.disconnect();
	}

	private void subscribeToChat(StompSession session, BlockingQueue<CreateMessageResponse> messageQueue) {

		session.subscribe(
				WebSocketPath.getChatRoomSubscribePath(chatRoom.getId()),
				new StompFrameHandler() {

					@Override
					public Type getPayloadType(StompHeaders headers) {

						return byte[].class;
					}

					@Override
					public void handleFrame(StompHeaders headers, Object payload) {

						try {
							byte[] messageBytes = (byte[]) payload;
							String jsonMessage = new String(messageBytes);
							CreateMessageResponse response = objectMapper.readValue(jsonMessage, CreateMessageResponse.class);
							messageQueue.offer(response);
						} catch (Exception e) {

							e.printStackTrace();
						}
					}
				});
	}

	private void subscribeToChat(StompSession session, BlockingQueue<CreateMessageResponse> messageQueue, Long chatRoomId) {

		session.subscribe(
				WebSocketPath.getChatRoomSubscribePath(chatRoomId),
				new StompFrameHandler() {

					@Override
					public Type getPayloadType(StompHeaders headers) {

						return byte[].class;
					}

					@Override
					public void handleFrame(StompHeaders headers, Object payload) {

						try {
							byte[] messageBytes = (byte[]) payload;
							String jsonMessage = new String(messageBytes);
							CreateMessageResponse response = objectMapper.readValue(jsonMessage, CreateMessageResponse.class);
							messageQueue.offer(response);
						} catch (Exception e) {

							e.printStackTrace();
						}
					}
				});
	}

	private void sendChatMessage(StompSession session, CreateChatMessageRequest request) {

		try {

			session.send(WebSocketPath.getChatRoomPublishPath(chatRoom.getId()), request);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private void sendChatMessage(StompSession session, CreateChatMessageRequest request, Long chatRoomId) {

		try {

			session.send(WebSocketPath.getChatRoomPublishPath(chatRoomId), request);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Test
	@Order(4)
	@DisplayName("구독하지 않은 채팅방으로의 메시지는 아무도 받지 않는다")
	void shouldHandleNonExistentChatRoom() throws Exception {

		//given
		Long nonExistentRoomId = 99999L;

		StompSession sellerStompClient = setupWebSocketClient(sellerToken);
		BlockingQueue<CreateMessageResponse> secondClientMessages = new LinkedBlockingDeque<>();

		//첫 번째 클라이언트 구독 - 존재하지 않는 채팅방
		subscribeToChat(buyerStompSession, receivedMessages, nonExistentRoomId);

		//두 번째 클라이언트 구독 - 존재하는 채팅방
		subscribeToChat(sellerStompClient, secondClientMessages);

		//구독이 완전히 설정될 때까지 대기
		Thread.sleep(1000);

		CreateChatMessageRequest request = new CreateChatMessageRequest("Message to non-existent room");

		//when - 존재하지 않는 채팅방으로 메시지 전송
		sendChatMessage(buyerStompSession, request, nonExistentRoomId);

		//then
		CreateMessageResponse buyerClientMessage = receivedMessages.poll(2, TimeUnit.SECONDS);
		CreateMessageResponse sellerClientMessage = secondClientMessages.poll(2, TimeUnit.SECONDS);

		//clean up
		sellerStompClient.disconnect();

		assertThat(buyerClientMessage).isNull();
		assertThat(sellerClientMessage).isNull();
	}

}
