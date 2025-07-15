package com.dapanda.member.entity;

import com.dapanda.auth.entity.OAuthProvider;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {

	public static Member createMember1() {

		return Member.ofOAuthMember(
				"dummy1@email.com",
				"dummy1Name",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);
	}

	public static Member createMember2() {

		return Member.ofOAuthMember(
				"dummy2@email.com",
				"dummy2Name",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);
	}

	public static Member createMember3() {

		return Member.ofOAuthMember(
				"dummy3@email.com",
				"dummy3Name",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);
	}

	public static Member createMember4() {

		return Member.ofOAuthMember(
				"dummy4@email.com",
				"dummy4Name",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);
	}

	public static Member createMember5() {

		return Member.ofOAuthMember(
				"dummy5@email.com",
				"dummy5Name",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);
	}

	public static Member createMember1WithId(Long memberId) {

		Member member = createMember1();

		ReflectionTestUtils.setField(member, "id", memberId);

		return member;
	}

	public static Member createMember2WithId(Long memberId) {

		Member member = createMember2();

		ReflectionTestUtils.setField(member, "id", memberId);

		return member;
	}
}
