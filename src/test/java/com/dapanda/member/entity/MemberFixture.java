package com.dapanda.member.entity;

public class MemberFixture {

    public static final Member MEMBER_REVIEWER = new Member(
            null,
            "더미 리뷰어1",
            "010-1234-5678",
            2000,
            0,
            MemberRole.ROLE_MEMBER,
            2500,
            false,
            0
    );

    public static final Member MEMBER_REVIEWEE = new Member(
            null,
            "더미 리뷰이1",
            "010-1234-1111",
            300,
            0,
            MemberRole.ROLE_MEMBER,
            2000,
            false,
            1
    );
}
