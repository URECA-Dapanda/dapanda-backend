package com.dapanda.member.entity;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

	private BigDecimal buyingData;

	private BigDecimal sellingData;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MemberRole role;

	private int cash;

	private boolean isBlocked;

	private int reportedCount;

	private int reviewCount;

	private float averageRating;

	private String profileImageUrl;

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

	public void resetDataAmount() {

		this.buyingData = BigDecimal.ZERO;
		this.sellingData = BigDecimal.ZERO;
  }
  
	public void updateMemberRole(MemberRole role) {

		this.role = role;
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

	public void addBuyingData(BigDecimal buyingData) {

		this.buyingData = this.buyingData.add(buyingData);
	}

	public void addSellingData(BigDecimal sellingData) {

		this.sellingData = this.sellingData.add(sellingData);
	}

	public void updateReviewInfo(int reviewCount, float averageRating) {

		this.reviewCount = reviewCount;
		this.averageRating = averageRating;
	}

	public void updateProfileImage(String profileImageUrl) {

		this.profileImageUrl = profileImageUrl;
	}
}
