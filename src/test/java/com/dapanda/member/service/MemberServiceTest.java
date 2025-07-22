package com.dapanda.member.service;

import static com.dapanda.TestConstants.Member.CASH_5000;
import static com.dapanda.TestConstants.Member.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.dapanda.member.dto.response.FindCashResponse;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("회원 서비스 테스트")
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberService memberService;

	@Nested
	@DisplayName("캐시 조회")
	class MobileDataFullDefaultPurchase {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 통합 상품 일반 구매를 성공한다")
			void findCash() throws Exception {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				ReflectionTestUtils.setField(member, "cash", CASH_5000);

				given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

				// when
				FindCashResponse response = memberService.findCash(MEMBER_ID);

				// then
				assertThat(response.getCash()).isEqualTo(CASH_5000);
			}
		}
	}
}
