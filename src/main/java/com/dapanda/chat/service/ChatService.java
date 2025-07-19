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

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		ChatRoom chatRoom = ChatRoom.createChatRoom(product);

		ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

		ChatParticipant seller = ChatParticipant.of(chatRoom, product.getMember());
		ChatParticipant buyer = ChatParticipant.of(chatRoom, member);

		chatParticipantRepository.saveAll(List.of(seller, buyer));

		return CreateChatRoomResponse.of(savedChatRoom.getId());
	}
}
