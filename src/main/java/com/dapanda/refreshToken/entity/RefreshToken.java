package com.dapanda.refreshToken.entity;

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
public class RefreshToken extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String token;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private TokenState state = TokenState.VALID;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Member member;

	public static RefreshToken of(String token, TokenState state, Member member) {

		return RefreshToken.builder()
				.token(token)
				.state(state)
				.member(member)
				.build();
	}

	public void deactivateToken(){

		this.state = TokenState.INVALID;
	}

}
