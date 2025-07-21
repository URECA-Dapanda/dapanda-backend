package com.dapanda.chat.entity;

import com.dapanda.product.entity.Product;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomFixture {

	public static ChatRoom createChatRoomWithId(Product product, Long chatRoomId) {

		ChatRoom chatRoom = ChatRoom.of(product);

		ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

		return chatRoom;
	}

	public static List<ChatRoom> createChatRoomList(List<Product> productList){

		List<ChatRoom> chatRoomList = new ArrayList<>();

		for (Product product : productList) {

			chatRoomList.add(ChatRoom.of(product));
		}

		return chatRoomList;
	}
}
