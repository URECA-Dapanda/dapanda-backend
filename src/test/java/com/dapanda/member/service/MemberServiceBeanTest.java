package com.dapanda.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberRole;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.refreshToken.repository.RefreshTokenRepository;
import com.dapanda.refreshToken.service.RefreshTokenService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayName("회원 서비스 테스트")
class MemberServiceBeanTest {

	static final String EMAIL = "user@aaa.com";
	static final String PASSWORD = "P@ssword1";
	static final String NAME = "홍길동";
	static final MemberRole ROLE = MemberRole.ROLE_MEMBER;

	@Autowired
	MemberService memberService;
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	RefreshTokenService refreshTokenService;
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	@Autowired
	RefreshTokenRepository refreshTokenRepository;

	@BeforeEach
	void setUp() {

		refreshTokenRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Nested
	@DisplayName("findUserByEmailAndProvider")
	class FindUserByEmailAndProviderTest {

		@Test
		@DisplayName("정상적으로 찾을 수 있다")
		void find_success() {

			Member member = Member.ofLocalMember(
					EMAIL,
					NAME,
					passwordEncoder.encode(PASSWORD),
					OAuthProvider.LOCAL,
					ROLE);
			memberRepository.save(member);

			Member found = memberService.findUserByEmailAndProvider(EMAIL, OAuthProvider.LOCAL);
			assertThat(found).isNotNull();
			assertThat(found.getEmail()).isEqualTo(EMAIL);
		}

		@Test
		@DisplayName("없는 경우 예외")
		void not_found() {

			GlobalException ex = assertThrows(GlobalException.class,
					() -> memberService.findUserByEmailAndProvider("none@none.com",
							OAuthProvider.LOCAL));
			assertThat(ex.getResultCode()).isEqualTo(ResultCode.MEMBER_NOT_FOUND);
		}
	}
}
