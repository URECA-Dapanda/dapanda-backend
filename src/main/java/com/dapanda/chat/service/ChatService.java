package com.dapanda.chat.service;

import com.dapanda.chat.dto.request.CreateChatMessageRequest;
import com.dapanda.chat.dto.request.ReadJoiningChatRoomRequest;
import com.dapanda.chat.dto.response.CreateChatRoomResponse;
import com.dapanda.chat.dto.response.ReadJoiningChatRoomResponse;
import com.dapanda.chat.entity.ChatMessage;
import com.dapanda.chat.entity.ChatParticipant;
import com.dapanda.chat.entity.ChatRoom;
import com.dapanda.chat.repository.ChatMessageRepository;
import com.dapanda.chat.repository.ChatParticipantRepository;
import com.dapanda.chat.repository.ChatRoomRepository;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.Product;
import com.dapanda.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatParticipantRepository chatParticipantRepository;
	private final ProductRepository productRepository;
	private final MemberRepository memberRepository;

	public CursorPageResponse<ReadJoiningChatRoomResponse> readChatRoom(ReadJoiningChatRoomRequest request) {

		List<ReadJoiningChatRoomResponse> response = chatRoomRepository.findJoiningChatRoom(request);

		boolean hasNext = response.size() > request.size();

		if (hasNext) {
			response = response.subList(0, request.size());
		}

		Long nextCursorId = hasNext && !response.isEmpty()
				? response.get(response.size() - 1).getChatRoomId()
				: null;

		CursorPageResponse.PageInfo pageInfo = CursorPageResponse.PageInfo.of(
				nextCursorId,
				hasNext,
				request.size()
		);

		return CursorPageResponse.of(response, pageInfo);
	}

	@Transactional
	public CreateChatRoomResponse createChatRoom(Long productId, Long memberId) {

		validateProductId(productId);
		validateOwnProductChatRoom(productId, memberId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new GlobalException(ResultCode.PRODUCT_NOT_FOUND));

		// 기존에 상품에 대해 나와 판매자의 1:1 채팅방이 생성되어 있는 경우, 기존 채팅방 아이디를 반환한다.
		Optional<Long> chatRoomId = chatRoomRepository.findExistingChatRoomIdOnProduct(
				product.getId(),
				product.getMember().getId(),
				memberId
		);

		if (chatRoomId.isPresent()) {

			return CreateChatRoomResponse.of(chatRoomId.get());
		}

		Member member = memberRepository.getReferenceById(memberId);

		ChatRoom chatRoom = ChatRoom.of(product);

		ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

		ChatParticipant seller = ChatParticipant.of(chatRoom, product.getMember());
		ChatParticipant buyer = ChatParticipant.of(chatRoom, member);

		chatParticipantRepository.saveAll(List.of(seller, buyer));

		return CreateChatRoomResponse.of(savedChatRoom.getId());
	}

	private void validateProductId(Long productId) {

		if (!productRepository.existsById(productId)) {

			throw new GlobalException(ResultCode.PRODUCT_NOT_FOUND);
		}
	}

	private void validateOwnProductChatRoom(Long productId, Long memberId) {

		if (productRepository.existsByIdAndMember_Id(productId, memberId)) {

			throw new GlobalException(ResultCode.CHAT_OWN_PRODUCT);
		}
	}

	public void createChatMessage(Long chatRoomId, CreateChatMessageRequest request) {

		validateParticipant(chatRoomId, request.senderId());

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new GlobalException(ResultCode.CHAT_ROOM_NOT_FOUND));

		Member sender = memberRepository.findById(request.senderId())
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		ChatMessage chatMessage = ChatMessage.of(request.message(), chatRoom, sender);

		chatMessageRepository.save(chatMessage);
	}

	private void validateParticipant(Long chatRoomId, Long memberId){

		if (!chatParticipantRepository.existsByChatRoom_IdAndMember_Id(chatRoomId, memberId)){

			throw new GlobalException(ResultCode.CHAT_ROOM_ACCESS_DENIED);
		}
	}
}
