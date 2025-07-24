package com.dapanda.chat.repository;

import com.dapanda.chat.dto.request.ReadChatMessageHistoryRequest;
import com.dapanda.chat.dto.response.SendChatMessageResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.dapanda.chat.entity.QChatMessage.chatMessage;
import static com.dapanda.chat.entity.QChatRoom.chatRoom;

@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<SendChatMessageResponse> findChatMessageHistory(ReadChatMessageHistoryRequest request) {

		BooleanBuilder whereClause = new BooleanBuilder();

		whereClause.and(chatRoom.id.eq(request.chatRoomId()));

		if (request.cursorId() != null && !request.cursorId().equals(0L)) {
			whereClause.and(chatMessage.id.lt(request.cursorId()));
		}

		return jpaQueryFactory
				.select(Projections.constructor(SendChatMessageResponse.class,
						chatMessage.id,
						chatMessage.member.id,
						chatMessage.message,
						chatMessage.createdAt
				))
				.from(chatMessage)
				.join(chatMessage.chatRoom, chatRoom)
				.where(whereClause)
				.orderBy(chatMessage.id.desc())
				.limit(request.size() + 1)
				.fetch();
	}
}
