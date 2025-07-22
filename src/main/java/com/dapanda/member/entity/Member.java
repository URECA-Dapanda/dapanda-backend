package com.dapanda.member.entity;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member",
		uniqueConstraints = @UniqueConstraint(columnNames = {"email", "provider"}))
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String name;

	private String password;

	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OAuthProvider provider;

	private float buyingData;

	private float sellingData;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MemberRole role;

	private int cash;

	private int point;

	private boolean isBlocked;

	private int reportedCount;

	private int reviewCount;

	private float averageRating;

	public static Member ofOAuthMember(String email, String name,
			OAuthProvider provider, MemberRole role) {

		return Member.builder()
				.email(email)
				.name(name)
				.provider(provider)
				.role(role)
				.build();
	}

	public static Member ofLocalMember(String email, String name,
			String password, OAuthProvider provider, MemberRole role
	) {

		return Member.builder()
				.email(email)
				.name(name)
				.password(password)
				.provider(provider)
				.role(role)
				.build();
	}

	public void increaseReportedCount() {

		this.reportedCount++;
	}

	public void addCash(int amount) {

		this.cash += amount;
	}

	public void deductCash(int amount) {

		this.cash -= amount;
	}

	// TODO: 매달 1일 초기화 메서드
	public void addBuyingData(float buyingData) {

		this.buyingData += buyingData;
	}

	public void addSellingData(float sellingData) {

		this.sellingData += sellingData;
	}

	public void updateReviewInfo(int reviewCount, float averageRating) {
		this.reviewCount = reviewCount;
		this.averageRating = averageRating;
	}
}
