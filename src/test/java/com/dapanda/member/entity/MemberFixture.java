package com.dapanda.member.entity;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.auth.entity.OAuthProvider;
import java.math.BigDecimal;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {

	public static Member createMember1() {

		Member member = Member.ofOAuthMember(
				"dummy1 + @email.com",
				"dummy1Name",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);

		ReflectionTestUtils.setField(member, "buyingData", BigDecimal.ZERO);
		ReflectionTestUtils.setField(member, "sellingData", BigDecimal.ZERO);

		member.updateProfileImage("test-image1.jpg");

		return member;
	}

	public static Member createMember2() {

		Member member = Member.ofOAuthMember(
				"dummy2@email.com",
				"dummy2Name",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);

		ReflectionTestUtils.setField(member, "buyingData", BigDecimal.ZERO);
		ReflectionTestUtils.setField(member, "sellingData", BigDecimal.ZERO);

		member.updateProfileImage("test-image2.jpg");

		return member;
	}

	public static Member createMember3() {

		Member member = Member.ofOAuthMember(
				"dummy3@email.com",
				"dummy3Name",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);

		ReflectionTestUtils.setField(member, "buyingData", BigDecimal.ZERO);
		ReflectionTestUtils.setField(member, "sellingData", BigDecimal.ZERO);

		return member;
	}

	public static Member createMember4() {

		Member member = Member.ofOAuthMember(
				"dummy4@email.com",
				"dummy4Name",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);

		ReflectionTestUtils.setField(member, "buyingData", BigDecimal.ZERO);
		ReflectionTestUtils.setField(member, "sellingData", BigDecimal.ZERO);

		return member;
	}

	public static Member createMember5() {

		Member member = Member.ofOAuthMember(
				"dummy5@email.com",
				"dummy5Name",
				OAuthProvider.KAKAO,
				MemberRole.ROLE_MEMBER
		);

		ReflectionTestUtils.setField(member, "buyingData", BigDecimal.ZERO);
		ReflectionTestUtils.setField(member, "sellingData", BigDecimal.ZERO);

		return member;
	}

	public static Member createMember1WithId(Long memberId) {

		Member member = createMember1();

		ReflectionTestUtils.setField(member, "id", memberId);
		ReflectionTestUtils.setField(member, "buyingData", BigDecimal.ZERO);
		ReflectionTestUtils.setField(member, "sellingData", BigDecimal.ZERO);

		return member;
	}

	public static Member createMember2WithId(Long memberId) {

		Member member = createMember2();

		ReflectionTestUtils.setField(member, "id", memberId);
		ReflectionTestUtils.setField(member, "buyingData", BigDecimal.ZERO);
		ReflectionTestUtils.setField(member, "sellingData", BigDecimal.ZERO);

		return member;
	}

	public static Member createMemberWithSellingDataWithId(long memberId, BigDecimal sellingData) {

		Member member = createMember1WithId(memberId);

		ReflectionTestUtils.setField(member, "sellingData", sellingData);  // 강제로 세팅
		ReflectionTestUtils.setField(member, "buyingData", BigDecimal.ZERO);

		return member;
	}

	public static Member createMemberWithSellingData(BigDecimal sellingData) {

		Member member = createMember1();

		ReflectionTestUtils.setField(member, "sellingData", sellingData);  // 강제로 세팅
		ReflectionTestUtils.setField(member, "buyingData", BigDecimal.ZERO);

		return member;
	}

	public static void setAuthentication(Long id, String email, String provider, MemberRole role) {
		CustomUserDetails userDetails = new CustomUserDetails(
				id, email, "password", provider, role
		);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken(userDetails,
				null);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}
