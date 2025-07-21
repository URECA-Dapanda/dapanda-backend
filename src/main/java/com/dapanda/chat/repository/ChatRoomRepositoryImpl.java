package com.dapanda.chat.repository;

import com.dapanda.chat.dto.request.ReadJoiningChatRoomRequest;
import com.dapanda.chat.dto.response.ReadJoiningChatRoomResponse;
import com.dapanda.chat.entity.ChatRoomReadOption;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.product.entity.ItemType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.dapanda.chat.entity.QChatParticipant.chatParticipant;
import static com.dapanda.chat.entity.QChatRoom.chatRoom;
import static com.dapanda.member.entity.QMember.member;
import static com.dapanda.product.entity.QMobileData.mobileData;
import static com.dapanda.product.entity.QProduct.product;
import static com.dapanda.product.entity.QWifi.wifi;

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

	@Override
	public List<ReadJoiningChatRoomResponse> findJoiningChatRoom(ReadJoiningChatRoomRequest request) {

		ChatRoomReadOption readOption = request.chatRoomReadOption();

		BooleanBuilder whereClause = new BooleanBuilder();

		JPAQuery<Tuple> query = jpaQueryFactory
				.select(
						chatRoom.id,
						chatRoom.createdAt,
						member.id,
						member.name,
						product.id,
						product.itemId,
						product.itemType,
						mobileData.dataAmount,
						mobileData.remainAmount,
						wifi.startTime,
						wifi.endTime
				)
				.from(chatParticipant)
				.join(chatParticipant.chatRoom, chatRoom)
				.join(chatParticipant.member, member)
				.join(chatRoom.product, product)
				.leftJoin(mobileData).on(
						product.itemId.eq(mobileData.id)
								.and(product.itemType.eq(ItemType.MOBILE_DATA))
				)
				.leftJoin(wifi).on(
						product.itemId.eq(wifi.id)
								.and(product.itemType.eq(ItemType.WIFI))
				)
				.where(chatParticipant.member.id.eq(request.memberId()));

		switch (readOption) {
			// 구매자 기준 채팅방 조회
			case BUYER -> whereClause.and(product.member.id.ne(request.memberId()));
			// 판매자 기준 채팅방 조회
			case SELLER -> whereClause.and(product.member.id.eq(request.memberId()));
			// 전체 채팅방 조회
			case ALL -> {
			}
		}

		List<Tuple> tuples = query
				.where(whereClause)
				.orderBy(chatRoom.lastMessageAt.desc(), chatRoom.createdAt.desc())
				.limit(request.size() + 1)
				.fetch();

		return tuples.stream()
				.map(tuple -> {

					ItemType itemType = tuple.get(product.itemType);

					if (itemType == null) {

						throw new GlobalException(ResultCode.INTERNAL_ERROR);
					}

					if (itemType.equals(ItemType.MOBILE_DATA)) {

						return ReadJoiningChatRoomResponse.createMobileDataChatRoomResponse(
								tuple.get(chatRoom.id),
								tuple.get(chatRoom.createdAt),
								tuple.get(member.id),
								tuple.get(member.name),
								tuple.get(product.id),
								tuple.get(product.itemId),
								itemType,
								tuple.get(mobileData.dataAmount),
								tuple.get(mobileData.remainAmount)
						);
					}

					return ReadJoiningChatRoomResponse.createWifiChatRoomResponse(
							tuple.get(chatRoom.id),
							tuple.get(chatRoom.createdAt),
							tuple.get(member.id),
							tuple.get(member.name),
							tuple.get(product.id),
							tuple.get(product.itemId),
							itemType,
							tuple.get(wifi.startTime),
							tuple.get(wifi.endTime)
					);
				})
				.toList();
	}
}
