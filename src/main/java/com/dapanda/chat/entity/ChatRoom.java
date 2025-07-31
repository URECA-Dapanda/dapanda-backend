package com.dapanda.chat.entity;

import com.dapanda.common.entity.CreatedAtEntity;
import com.dapanda.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private ChatRoomState state;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private Product product;

	private LocalDateTime lastMessageAt;

	private String lastMessage;

	public static ChatRoom of(Product product){

		return ChatRoom.builder()
				.state(ChatRoomState.OPENED)
				.product(product)
				.lastMessageAt(LocalDateTime.now())
				.build();
	}

	public void updateLastMessage(ChatMessage chatMessage) {

		this.lastMessageAt = chatMessage.getCreatedAt();
		this.lastMessage = chatMessage.getMessage();
	}
}
