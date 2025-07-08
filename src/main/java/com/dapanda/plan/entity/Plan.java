package com.dapanda.plan.entity;

import com.dapanda.common.entity.BaseEntity;
import com.dapanda.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
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
    private Member member;
}
