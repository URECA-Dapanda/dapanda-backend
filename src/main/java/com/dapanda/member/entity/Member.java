package com.dapanda.member.entity;

import com.dapanda.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String phoneNumber;

    private int buyingData;

    private int sellingData;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private int cash;

    private boolean isBlocked;

    private int reportedCount;

    public void addCash(int amount) {

        this.cash += amount;
    }

    public void deductCash(int amount) {

        this.cash -= amount;
    }
}
