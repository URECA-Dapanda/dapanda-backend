package com.dapanda.chat.entity;

import com.dapanda.chat.dto.response.ReadJoiningChatRoomResponse;
import com.dapanda.product.entity.ItemType;
import com.dapanda.product.entity.Product;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomFixture {

	public static ChatRoom createChatRoomWithId(Product product, Long chatRoomId) {

		ChatRoom chatRoom = ChatRoom.of(product);

		ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

		return chatRoom;
	}

	public static ChatRoom createChatRoom(Product product) {

		return ChatRoom.of(product);
	}

	public static List<ChatRoom> createChatRoomList(List<Product> productList){

		List<ChatRoom> chatRoomList = new ArrayList<>();

		for (Product product : productList) {

			chatRoomList.add(ChatRoom.of(product));
		}

		return chatRoomList;
	}

	public static List<ReadJoiningChatRoomResponse> create2ReadJoiningChatResponse() {

		LocalDateTime now = LocalDateTime.now();

		ReadJoiningChatRoomResponse mobileDataChatRoom = ReadJoiningChatRoomResponse.of(
				1L,
				now.minusDays(5),
				now.minusMinutes(10),
				"감사합니다",
				100L,
				"판매자1",
				1000L,
				2000L,
				ItemType.MOBILE_DATA,
				now.plusHours(1),
				now.plusHours(3)
		);

		ReadJoiningChatRoomResponse wifiChatRoom1 = ReadJoiningChatRoomResponse.of(
				2L,
				now.minusDays(3),
				now.minusMinutes(5),
				"감사합니다",
				101L,
				"판매자2",
				1001L,
				2001L,
				ItemType.WIFI,
				now.plusHours(1),
				now.plusHours(3)
		);

		ReadJoiningChatRoomResponse wifiChatRoom2 = ReadJoiningChatRoomResponse.of(
				3L,
				now.minusDays(4),
				now.minusMinutes(6),
				"감사합니다",
				102L,
				"판매자3",
				1004L,
				2005L,
				ItemType.WIFI,
				now.plusHours(1),
				now.plusHours(3)
		);

		return List.of(wifiChatRoom2, wifiChatRoom1, mobileDataChatRoom);
	}
}
