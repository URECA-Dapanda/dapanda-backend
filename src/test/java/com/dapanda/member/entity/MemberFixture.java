package com.dapanda.member.entity;

import com.dapanda.auth.entity.OAuthProvider;

public class MemberFixture {

    public static final Member MEMBER_REVIEWER = Member.ofOAuthMember(
            "dummy1@email.com",
            "dummy1Name",
            OAuthProvider.KAKAO,
            MemberRole.ROLE_MEMBER
    );

    public static final Member MEMBER_REVIEWEE = Member.ofOAuthMember(
            "dummy2@email.com",
            "dummy2Name",
            OAuthProvider.KAKAO,
            MemberRole.ROLE_MEMBER
    );
}
