package com.dapanda.chat.entity;

import com.dapanda.product.entity.Product;
import org.springframework.test.util.ReflectionTestUtils;

public class ChatRoomFixture {

	public static ChatRoom createChatRoomWithId(Product product, Long chatRoomId) {

		ChatRoom chatRoom = ChatRoom.of(product);

		ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

		return chatRoom;
	}
}
