package com.dapanda.chat.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.dapanda.chat.entity.QChatParticipant.chatParticipant;
import static com.dapanda.chat.entity.QChatRoom.chatRoom;

@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Optional<Long> findExistingChatRoomIdOnProduct(Long productId, Long sellerId, Long buyerId) {

		Long chatRoomId = jpaQueryFactory
				.select(chatRoom.id)
				.from(chatParticipant)
				.join(chatParticipant.chatRoom, chatRoom)
				.where(chatRoom.product.id.eq(productId)
						.and(chatParticipant.member.id.in(sellerId, buyerId)))
				.groupBy(chatRoom.id)
				.having(chatParticipant.member.id.countDistinct().eq(2L))
				.fetchFirst();

		return Optional.ofNullable(chatRoomId);
	}
}
