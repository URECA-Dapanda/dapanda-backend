package com.dapanda.member.entity;

import com.dapanda.auth.entity.OAuthProvider;

public class MemberFixture {

	public static Member MEMBER1 = Member.ofOAuthMember(
			"dummy1@email.com",
			"dummy1Name",
			OAuthProvider.KAKAO,
			MemberRole.ROLE_MEMBER
	);

	public static Member MEMBER2 = Member.ofOAuthMember(
			"dummy2@email.com",
			"dummy2Name",
			OAuthProvider.KAKAO,
			MemberRole.ROLE_MEMBER
	);

	public static Member createMember1() {

		return Member.ofOAuthMember(
				"test1@email.com",
				"test1",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);
	}
}
