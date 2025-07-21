package com.dapanda.chat.dto.response;

import com.dapanda.product.entity.ItemType;
import com.fasterxml.jackson.annotation.JsonInclude;
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

	//Member
	private Long senderId;
	private String senderName;

	//Product
	private Long productId;
	private Long itemId;
	private ItemType itemType;

	//Item

	//mobileData
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Float dataAmount;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Float remainAmount;

	//wifi
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private LocalDateTime startTime;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private LocalDateTime endTime;

	public static ReadJoiningChatRoomResponse createMobileDataChatRoomResponse(
			Long chatRoomId,
			LocalDateTime createdAt,
			Long senderId,
			String senderName,
			Long productId,
			Long itemId,
			ItemType itemType,
			Float dataAmount,
			Float remainAmount) {

		return ReadJoiningChatRoomResponse.builder()
				.chatRoomId(chatRoomId)
				.createdAt(createdAt)
				.senderId(senderId)
				.senderName(senderName)
				.productId(productId)
				.itemId(itemId)
				.itemType(itemType)
				.dataAmount(dataAmount)
				.remainAmount(remainAmount)
				.build();
	}

	public static ReadJoiningChatRoomResponse createWifiChatRoomResponse(
			Long chatRoomId,
			LocalDateTime createdAt,
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
