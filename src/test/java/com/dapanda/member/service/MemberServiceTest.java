package com.dapanda.member.service;

import static com.dapanda.TestConstants.Member.BUYING_DATA;
import static com.dapanda.TestConstants.Member.CASH_5000;
import static com.dapanda.TestConstants.Member.MEMBER_ID;
import static com.dapanda.TestConstants.Member.SELLING_DATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.dapanda.member.dto.response.FindCashResponse;
import com.dapanda.member.dto.response.FindDataResponse;
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
	class FindCash {

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

	@Nested
	@DisplayName("구매/판매 데이터양 조회")
	class findPurchaseSaleData {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("회원의 구매 데이터양 조회를 성공한다")
			void findBuyingCash() throws Exception {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				ReflectionTestUtils.setField(member, "buyingData", BUYING_DATA);

				given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

				// when
				FindDataResponse response = memberService.findBuyingData(MEMBER_ID);

				// then
				assertThat(response.getData()).isEqualTo(BUYING_DATA);
			}

			@Test
			@DisplayName("회원의 판매 데이터양 조회를 성공한다")
			void findSellingCash() throws Exception {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				ReflectionTestUtils.setField(member, "sellingData", SELLING_DATA);

				given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

				// when
				FindDataResponse response = memberService.findSellingData(MEMBER_ID);

				// then
				assertThat(response.getData()).isEqualTo(SELLING_DATA);
			}
		}
	}
}
