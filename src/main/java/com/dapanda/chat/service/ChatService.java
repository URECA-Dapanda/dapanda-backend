package com.dapanda.chat.service;

import com.dapanda.chat.dto.response.CreateChatRoomResponse;
import com.dapanda.chat.entity.ChatParticipant;
import com.dapanda.chat.entity.ChatRoom;
import com.dapanda.chat.repository.ChatMessageRepository;
import com.dapanda.chat.repository.ChatParticipantRepository;
import com.dapanda.chat.repository.ChatRoomRepository;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.Product;
import com.dapanda.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

	public CreateChatRoomResponse createChatRoom(Long productId, Long memberId) {

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

		validateOwnProductChatRoom(product, memberId);

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		ChatRoom chatRoom = ChatRoom.of(product);

		ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

		ChatParticipant seller = ChatParticipant.of(chatRoom, product.getMember());
		ChatParticipant buyer = ChatParticipant.of(chatRoom, member);

		chatParticipantRepository.saveAll(List.of(seller, buyer));

		return CreateChatRoomResponse.of(savedChatRoom.getId());
	}

	private void validateOwnProductChatRoom(Product product, Long memberId) {

		if (product.getMember().getId().equals(memberId)) {

			throw new GlobalException(ResultCode.CHAT_OWN_PRODUCT);
		}
	}
}
