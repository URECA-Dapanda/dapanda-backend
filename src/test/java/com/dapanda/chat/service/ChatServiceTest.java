package com.dapanda.chat.service;

import com.dapanda.chat.dto.request.CreateChatMessageRequest;
import com.dapanda.chat.dto.request.ReadJoiningChatRoomRequest;
import com.dapanda.chat.dto.response.CreateChatRoomResponse;
import com.dapanda.chat.dto.response.ReadJoiningChatRoomResponse;
import com.dapanda.chat.entity.*;
import com.dapanda.chat.repository.ChatMessageRepository;
import com.dapanda.chat.repository.ChatParticipantRepository;
import com.dapanda.chat.repository.ChatRoomRepository;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.Product;
import com.dapanda.product.entity.ProductFixture;
import com.dapanda.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.dapanda.TestConstants.Chat.*;
import static com.dapanda.TestConstants.Member.*;
import static com.dapanda.TestConstants.Pagination.DEFAULT_SIZE_2;
import static com.dapanda.TestConstants.Product.PRODUCT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("채팅 서비스 테스트")
public class ChatServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ChatParticipantRepository chatParticipantRepository;

	@Mock
	private ChatMessageRepository chatMessageRepository;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@InjectMocks
	private ChatService chatService;

	@Nested
	@DisplayName("채팅방 생성")
	class CreateChatRoom {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("생성된 채팅방 아이디를 반환한다")

			public void createChatRoomTest() {

				//given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);

				Product product = ProductFixture.createProduct1WithId(seller, PRODUCT_ID);

				ChatRoom chatRoom = ChatRoomFixture.createChatRoomWithId(product, CHAT_ROOM_ID);

				ChatParticipant chatParticipant1 = ChatParticipantFixture.createChatParticipantWithId(chatRoom, seller, CHAT_PARTICIPANT_ID_1);
				ChatParticipant chatParticipant2 = ChatParticipantFixture.createChatParticipantWithId(chatRoom, buyer, CHAT_PARTICIPANT_ID_2);

				given(productRepository.existsById(product.getId())).willReturn(true);
				given(productRepository.existsByIdAndMember_Id(product.getId(), buyer.getId())).willReturn(false);
				given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
				given(memberRepository.getReferenceById(buyer.getId())).willReturn(buyer);
				given(chatRoomRepository.findExistingChatRoomIdOnProduct(product.getId(), product.getMember().getId(), buyer.getId())).willReturn(Optional.empty());
				given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);
				given(chatParticipantRepository.saveAll(anyList())).willReturn(List.of(chatParticipant1, chatParticipant2));

				//when
				CreateChatRoomResponse response = chatService.createChatRoom(product.getId(), buyer.getId());

				//then
				assertThat(response.getChatRoomId()).isEqualTo(chatRoom.getId());
			}

			@Test
			@DisplayName("이미 생성된 채팅방이 있다면 기존 채팅방 아이디를 반환한다")
			public void returnPreviousChatRoomTest() {

				//given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);

				Product product = ProductFixture.createProduct1WithId(seller, PRODUCT_ID);

				ChatRoom chatRoom = ChatRoomFixture.createChatRoomWithId(product, CHAT_ROOM_ID);

				given(productRepository.existsById(product.getId())).willReturn(true);
				given(productRepository.existsByIdAndMember_Id(product.getId(), buyer.getId())).willReturn(false);
				given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
				given(chatRoomRepository.findExistingChatRoomIdOnProduct(product.getId(), product.getMember().getId(), buyer.getId())).willReturn(Optional.of(chatRoom.getId()));

				//when
				CreateChatRoomResponse response = chatService.createChatRoom(product.getId(), buyer.getId());

				//then
				assertThat(response.getChatRoomId()).isEqualTo(chatRoom.getId());

				verify(chatRoomRepository, never()).save(any(ChatRoom.class));
				verify(chatParticipantRepository, never()).saveAll(anyList());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("상품 아이디가 존재하지 않으면 예외가 발생한다")
			public void validateProductIdTest() {

				//given
				given(productRepository.existsById(PRODUCT_ID)).willReturn(false);

				//when & then
				assertThatThrownBy(() -> chatService.createChatRoom(PRODUCT_ID, USER_DETAILS_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.PRODUCT_NOT_FOUND.getMessage());
			}

			@Test
			@DisplayName("자신의 상품에 대해 채팅방을 생성하려고 하면 예외가 발생한다")
			public void validateOwnProductChatRoomTest() {

				//given
				given(productRepository.existsById(PRODUCT_ID)).willReturn(true);
				given(productRepository.existsByIdAndMember_Id(PRODUCT_ID, BUYER_MEMBER_ID)).willReturn(true);

				//when & then
				assertThatThrownBy(() -> chatService.createChatRoom(PRODUCT_ID, BUYER_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.CHAT_OWN_PRODUCT.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("채팅 메시지 저장")
	class CreateChatMessage {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("채팅 메시지가 저장된다")
			public void createChatMessageTest() {

				//given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember2WithId(BUYER_MEMBER_ID);

				Product product = ProductFixture.createProduct1WithId(seller, PRODUCT_ID);

				ChatRoom chatRoom = ChatRoomFixture.createChatRoomWithId(product, CHAT_ROOM_ID);

				ChatMessage chatMessage = ChatMessageFixture.createChatMessageWithId(chatRoom, buyer, CHAT_MESSAGE_ID);

				CreateChatMessageRequest request = new CreateChatMessageRequest(buyer.getId(), CHAT_MESSAGE);

				given(chatParticipantRepository.existsByChatRoom_IdAndMember_Id(chatRoom.getId(), buyer.getId())).willReturn(true);
				given(chatRoomRepository.findById(chatRoom.getId())).willReturn(Optional.of(chatRoom));
				given(memberRepository.findById(request.senderId())).willReturn(Optional.of(buyer));
				given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(chatMessage);

				//when
				chatService.createChatMessage(chatRoom.getId(), request);

				//then
				verify(chatMessageRepository).save(any(ChatMessage.class));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("채팅방 참가자가 아닐 경우 예외가 발생한다")
			public void validateParticipantTest() {

				//given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember2WithId(BUYER_MEMBER_ID);

				Product product = ProductFixture.createProduct1WithId(seller, PRODUCT_ID);

				ChatRoom chatRoom = ChatRoomFixture.createChatRoomWithId(product, CHAT_ROOM_ID);

				CreateChatMessageRequest request = new CreateChatMessageRequest(buyer.getId(), CHAT_MESSAGE);

				given(chatParticipantRepository.existsByChatRoom_IdAndMember_Id(chatRoom.getId(), buyer.getId())).willReturn(false);


				//when & then
				assertThatThrownBy(() -> chatService.createChatMessage(chatRoom.getId(), request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.CHAT_ROOM_ACCESS_DENIED.getMessage());

				//then
				verify(chatMessageRepository, never()).save(any(ChatMessage.class));
			}

			@Test
			@DisplayName("채팅방이 존재하지 않을 경우 예외가 발생한다")
			public void validateChatRoomIdTest() {

				//given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember2WithId(BUYER_MEMBER_ID);

				Product product = ProductFixture.createProduct1WithId(seller, PRODUCT_ID);

				ChatRoom chatRoom = ChatRoomFixture.createChatRoomWithId(product, CHAT_ROOM_ID);

				CreateChatMessageRequest request = new CreateChatMessageRequest(buyer.getId(), CHAT_MESSAGE);

				given(chatParticipantRepository.existsByChatRoom_IdAndMember_Id(chatRoom.getId(), buyer.getId())).willReturn(true);
				given(chatRoomRepository.findById(chatRoom.getId())).willReturn(Optional.empty());

				//when & then
				assertThatThrownBy(() -> chatService.createChatMessage(chatRoom.getId(), request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.CHAT_ROOM_NOT_FOUND.getMessage());

				//then
				verify(chatMessageRepository, never()).save(any(ChatMessage.class));
			}

			@Test
			@DisplayName("회원이 존재하지 않을 경우 예외가 발생한다")
			public void validateMemberIdTest() {

				//given
				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember2WithId(BUYER_MEMBER_ID);

				Product product = ProductFixture.createProduct1WithId(seller, PRODUCT_ID);

				ChatRoom chatRoom = ChatRoomFixture.createChatRoomWithId(product, CHAT_ROOM_ID);

				CreateChatMessageRequest request = new CreateChatMessageRequest(buyer.getId(), CHAT_MESSAGE);

				given(chatParticipantRepository.existsByChatRoom_IdAndMember_Id(chatRoom.getId(), buyer.getId())).willReturn(true);
				given(chatRoomRepository.findById(chatRoom.getId())).willReturn(Optional.of(chatRoom));
				given(memberRepository.findById(request.senderId())).willReturn(Optional.empty());

				//when & then
				assertThatThrownBy(() -> chatService.createChatMessage(chatRoom.getId(), request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.MEMBER_NOT_FOUND.getMessage());

				//then
				verify(chatMessageRepository, never()).save(any(ChatMessage.class));
			}
		}
	}

	@Nested
	@DisplayName("채팅방 조회")
	class ReadChatRoom {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("커서 기반 페이징 응답을 반환한다")
			public void readChatRoomTest() {

				//given
				ReadJoiningChatRoomRequest request = new ReadJoiningChatRoomRequest(
						null,
						null,
						DEFAULT_SIZE_2,
						ChatRoomReadOption.ALL,
						USER_DETAILS_MEMBER_ID
				);

				List<ReadJoiningChatRoomResponse> response = ChatRoomFixture.create2ReadJoiningChatResponse();


				given(chatRoomRepository.findJoiningChatRoom(request)).willReturn(response);

				//when
				CursorPageResponse<ReadJoiningChatRoomResponse> pageResponse = chatService.readChatRoom(request);

				//then
				assertThat(pageResponse.getData().size()).isEqualTo(DEFAULT_SIZE_2);
				assertThat(pageResponse.getData().get(0)).isEqualTo(response.get(0));
				assertThat(pageResponse.getData().get(1)).isEqualTo(response.get(1));

				assertThat(pageResponse.getPageInfo().isHasNext()).isTrue();
				assertThat(pageResponse.getPageInfo().getNextCursorId()).isEqualTo(response.get(DEFAULT_SIZE_2 - 1).getChatRoomId());
				assertThat(pageResponse.getPageInfo().getSize()).isEqualTo(DEFAULT_SIZE_2);
			}
		}

	}
}
