package com.dapanda.member.entity;

import com.dapanda.auth.entity.OAuthProvider;
import org.springframework.test.util.ReflectionTestUtils;

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

	public static Member createMember1(Long memberId) {

		Member member = Member.ofOAuthMember(
				"test1@email.com",
				"test1",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);

		ReflectionTestUtils.setField(member, "id", memberId);

		return member;
	}

    public static Member createMember2(Long memberId) {

        Member member = Member.ofOAuthMember(
                "test2@email.com",
                "test2",
                OAuthProvider.KAKAO,
                MemberRole.ROLE_MEMBER
        );

        ReflectionTestUtils.setField(member, "id", memberId);

        return member;
    }
}
