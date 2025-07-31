package com.dapanda.plan.entity;

import com.dapanda.common.entity.BaseEntity;
import com.dapanda.member.entity.Member;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
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

	private BigDecimal currentDataAmount;

	private BigDecimal providingDataAmount;

	private int monthlyPrice;

	@Enumerated(EnumType.STRING)
	private PlanCategory category;

	@Enumerated(EnumType.STRING)
	private AgeGroup ageGroup;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, unique = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Member member;

	public static Plan of(String name, BigDecimal providingDataAmount, int monthlyPrice,
			PlanCategory category, AgeGroup ageGroup, Member member) {

		return Plan.builder()
				.name(name)
				.currentDataAmount(providingDataAmount)
				.providingDataAmount(providingDataAmount)
				.monthlyPrice(monthlyPrice)
				.category(category)
				.ageGroup(ageGroup)
				.member(member)
				.build();
	}

	public void addMobileData(BigDecimal dataAmount) {

		this.currentDataAmount = this.currentDataAmount.add(dataAmount);
	}

	public void deductMobileData(BigDecimal dataAmount) {

		this.currentDataAmount = this.currentDataAmount.subtract(dataAmount);
	}
}
