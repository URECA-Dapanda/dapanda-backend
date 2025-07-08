package com.dapanda.member.entity;

import com.dapanda.common.entity.BaseEntity;
import jakarta.persistence.*;
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

    private int point;

    private boolean isBlocked;

    private int reportedCount;

}
