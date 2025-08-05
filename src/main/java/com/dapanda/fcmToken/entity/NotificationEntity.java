package com.dapanda.fcmToken.entity;

import com.dapanda.common.entity.CreatedAtEntity;
import com.dapanda.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String body;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Member member;

	public static NotificationEntity of(String title, String body, Member member) {

		return NotificationEntity.builder()
				.title(title)
				.body(body)
				.member(member)
				.build();
	}
}
