package com.dapanda.fcmToken.dto.response;

import com.dapanda.fcmToken.entity.NotificationEntity;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationResponse {

	private Long id;
	private String title;
	private String body;
	private LocalDateTime createdAt;

	public static NotificationResponse of(Long id, String title, String body,
			LocalDateTime createdAt) {

		return NotificationResponse.builder()
				.id(id)
				.title(title)
				.body(body)
				.createdAt(createdAt)
				.build();
	}

	public static NotificationResponse from(NotificationEntity notification) {

		return NotificationResponse.builder()
				.id(notification.getId())
				.title(notification.getTitle())
				.body(notification.getBody())
				.createdAt(notification.getCreatedAt())
				.build();
	}
}
