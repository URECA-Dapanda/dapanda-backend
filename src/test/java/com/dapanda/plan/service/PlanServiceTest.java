package com.dapanda.plan.service;

import static com.dapanda.TestConstants.Member.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.plan.dto.response.PlanInfoResponse;
import com.dapanda.plan.entity.*;
import com.dapanda.plan.repository.PlanRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("요금제 서비스 테스트")
class PlanServiceTest {

	@Mock
	private PlanRepository planRepository;

	@InjectMocks
	private PlanService planService;

	private Member member;

	@BeforeEach
	void setUp() {
		member = mock(Member.class);
	}

	@Nested
	@DisplayName("createRandomPlanForMember")
	class CreateRandomPlanForMemberTest {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("요금제가 없는 회원에게 요금제를 생성해서 반환한다")
			void shouldCreateAndReturnPlanForMemberWithoutPlan() {
				// given
				when(planRepository.existsByMember(member)).thenReturn(false);

				ArgumentCaptor<Plan> planCaptor = ArgumentCaptor.forClass(Plan.class);
				when(planRepository.save(planCaptor.capture()))
						.thenAnswer(invocation -> invocation.getArgument(0));

				// when
				Plan result = planService.createRandomPlanForMember(member);

				// then
				assertThat(result).isNotNull();
				assertThat(result.getMember()).isEqualTo(member);
				assertThat(result.getName()).isNotBlank();
				assertThat(result.getCurrentDataAmount()).isPositive();
				assertThat(result.getMonthlyPrice()).isPositive();
				assertThat(result.getCategory()).isInstanceOf(PlanCategory.class);
				assertThat(result.getAgeGroup()).isInstanceOf(AgeGroup.class);

				verify(planRepository, times(1)).save(any(Plan.class));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("이미 요금제가 있는 경우 기존 요금제를 반환한다")
			void shouldReturnExistingPlanIfAlreadyExists() {
				// given
				Plan existingPlan = mock(Plan.class);
				when(planRepository.existsByMember(member)).thenReturn(true);
				when(planRepository.findByMember(member)).thenReturn(Optional.of(existingPlan));

				// when
				Plan result = planService.createRandomPlanForMember(member);

				// then
				assertThat(result).isEqualTo(existingPlan);
				verify(planRepository, never()).save(any(Plan.class));
			}
		}
	}

	@Nested
	@DisplayName("findMobileDataInfoByMemberId")
	class FindMobileDataInfoByMemberIdTest {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("해당 memberId에 요금제가 있으면 PlanInfoResponse를 반환한다")
			void shouldReturnPlanInfoResponseWhenPlanExists() {
				// given
				Long memberId = 1L;
				Plan plan = mock(Plan.class);

				when(planRepository.findByMemberId(memberId)).thenReturn(Optional.of(plan));
				when(plan.getName()).thenReturn("프리미엄");
				when(plan.getCurrentDataAmount()).thenReturn(BigDecimal.valueOf(30.0));
				when(plan.getProvidingDataAmount()).thenReturn(BigDecimal.valueOf(30.0));
				when(plan.getMonthlyPrice()).thenReturn(25000);

				// when
				PlanInfoResponse result = planService.findMobileDataInfoByMemberId(memberId);

				// then
				assertThat(result).isNotNull();
				assertThat(result.getName()).isEqualTo("프리미엄");
				assertThat(result.getCurrentDataAmount()).isEqualByComparingTo(
						BigDecimal.valueOf(30.0));
				assertThat(result.getProvidingDataAmount()).isEqualByComparingTo(
						BigDecimal.valueOf(30.0));
				assertThat(result.getMonthlyPrice()).isEqualTo(25000);
				verify(planRepository, times(1)).findByMemberId(memberId);
			}

		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("해당 memberId에 요금제가 없으면 예외(GlobalException)를 던진다")
			void shouldThrowExceptionWhenPlanNotExists() {
				// given
				Long memberId = 2L;
				when(planRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

				// when & then
				org.junit.jupiter.api.Assertions.assertThrows(
						com.dapanda.common.exception.GlobalException.class,
						() -> planService.findMobileDataInfoByMemberId(memberId)
				);
				verify(planRepository, times(1)).findByMemberId(memberId);
			}
		}
	}

	@Nested
	@DisplayName("회원의 요금제 데이터 초기화")
	class resetMemberMobileDataPlan {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("회원의 요금제 데이터를 초기화한다")
			void resetMemberMobileDataPlan() throws Exception {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				Plan plan = PlanFixture.createPlan(member, BigDecimal.valueOf(10.0));

				// when
				planService.resetMemberMobileDataPlan();

				// then
				verify(planRepository).resetMemberMobileDataPlan();
			}
		}
	}

}
