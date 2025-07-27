package com.dapanda.chat.dto.response;

import com.dapanda.product.entity.ItemType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ReadJoiningChatRoomResponse {

	//ChatRoom
	private Long chatRoomId;
	private LocalDateTime createdAt;
	private LocalDateTime lastMessageAt;

	//Member
	private Long senderId;
	private String senderName;
	private String senderProfileImageUrl;

	//Product
	private Long productId;
	private Long itemId;
	private ItemType itemType;

	//wifi
	private LocalDateTime startTime;
	private LocalDateTime endTime;

	public static ReadJoiningChatRoomResponse of(
			Long chatRoomId,
			LocalDateTime createdAt,
			LocalDateTime lastMessageAt,
			Long senderId,
			String senderName,
			Long productId,
			Long itemId,
			ItemType itemType,
			LocalDateTime startTime,
			LocalDateTime endTime) {

		return ReadJoiningChatRoomResponse.builder()
				.chatRoomId(chatRoomId)
				.createdAt(createdAt)
				.lastMessageAt(lastMessageAt)
				.senderId(senderId)
				.senderName(senderName)
				.productId(productId)
				.itemId(itemId)
				.itemType(itemType)
				.startTime(startTime)
				.endTime(endTime)
				.build();
	}
}
