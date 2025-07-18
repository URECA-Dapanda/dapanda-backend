package com.dapanda.plan.entity;

import com.dapanda.common.entity.BaseEntity;
import com.dapanda.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private int providingDataAmount;

	private int monthlyPrice;

	@Enumerated(EnumType.STRING)
	private PlanCategory category;

	@Enumerated(EnumType.STRING)
	private AgeGroup ageGroup;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, unique = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Member member;

	public static Plan of(String name, int providingDataAmount, int monthlyPrice,
			PlanCategory category, AgeGroup ageGroup, Member member) {

		return Plan.builder()
				.name(name)
				.providingDataAmount(providingDataAmount)
				.monthlyPrice(monthlyPrice)
				.category(category)
				.ageGroup(ageGroup)
				.member(member)
				.build();
	}
}
