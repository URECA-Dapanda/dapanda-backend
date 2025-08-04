package com.dapanda.report.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.entity.*;
import com.dapanda.report.dto.request.CreateReportRequest;
import com.dapanda.report.dto.response.CreateReportResponse;
import com.dapanda.report.entity.Report;
import com.dapanda.report.entity.ReportFixture;
import com.dapanda.report.repository.ReportRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.dapanda.TestConstants.Member.*;
import static com.dapanda.TestConstants.Product.PRODUCT_ID;
import static com.dapanda.TestConstants.Report.*;
import static com.dapanda.TestConstants.Wifi.WIFI_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("신고 서비스 테스트")
class ReportServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ReportRepository reportRepository;

	@InjectMocks
	private ReportService reportService;

	@Nested
	@DisplayName("신고 생성")
	class CreateReport {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("신고 생성이 완료되면 신고 아이디를 반환한다")
			public void createReportTest() {

				//given
				CreateReportRequest request = new CreateReportRequest(REASON, REPORT_TARGET_CATEGORY_PRODUCT);

				Member reporter = MemberFixture.createMember1WithId(USER_DETAILS_MEMBER_ID);
				Member reportedMember = MemberFixture.createMember2WithId(SELLER_MEMBER_ID);

				Product product = ProductFixture.createProduct1WithId(reportedMember, PRODUCT_ID);

				Report savedReport = ReportFixture.createReportFromProductWithId(product, request.targetCategory(), reporter, REPORT_ID);

				given(memberRepository.getReferenceById(USER_DETAILS_MEMBER_ID)).willReturn(reporter);
				given(reportRepository.existsByReportTargetIdAndReportTargetCategoryAndReporterId(TARGET_ID, REPORT_TARGET_CATEGORY_PRODUCT, USER_DETAILS_MEMBER_ID)).willReturn(false);
				given(reportRepository.save(any(Report.class))).willReturn(savedReport);
				given(memberRepository.findMemberIdByProductId(PRODUCT_ID)).willReturn(Optional.of(SELLER_MEMBER_ID));
				given(memberRepository.findByIdForUpdate(SELLER_MEMBER_ID)).willReturn(Optional.of(reportedMember));

				//when
				CreateReportResponse response = reportService.createReport(PRODUCT_ID, USER_DETAILS_MEMBER_ID, request);

				//then
				assertThat(response.getReportId()).isEqualTo(REPORT_ID);

				verify(reportRepository).save(any());
			}

			@Test
			@DisplayName("신고가 5회 이상이면 신고 받은 회원이 차단된다.")
			public void blockedStateTest() {

				//given
				CreateReportRequest request = new CreateReportRequest(REASON, REPORT_TARGET_CATEGORY_PRODUCT);

				Member seller = MemberFixture.createBlockedMemberWithId(SELLER_MEMBER_ID);
				Member buyer = MemberFixture.createMember1WithId(BUYER_MEMBER_ID);

				Wifi wifi = WifiFixture.createWifiWithId(WIFI_ID);

				Product product = ProductFixture.createWifiProductWithId(wifi, seller, PRODUCT_ID);

				Report report = ReportFixture.createReportFromProductWithId(product, request.targetCategory(), buyer, REPORT_ID);

				given(memberRepository.getReferenceById(buyer.getId())).willReturn(buyer);
				given(reportRepository.existsByReportTargetIdAndReportTargetCategoryAndReporterId(
						product.getId(), request.targetCategory(), buyer.getId())).willReturn(false);
				given(reportRepository.save(any(Report.class))).willReturn(report);
				given(memberRepository.findMemberIdByProductId(product.getId())).willReturn(Optional.of(seller.getId()));
				given(memberRepository.findByIdForUpdate(seller.getId())).willReturn(Optional.of(seller));

				//when
				CreateReportResponse response = reportService.createReport(product.getId(), buyer.getId(), request);

				//then
				assertThat(response.getReportId()).isEqualTo(report.getId());
				assertThat(seller.isBlocked()).isEqualTo(true);

				verify(reportRepository).save(any());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("동일한 회원이 동일한 대상에게 중복 신고를 할 수 없습니다")
			public void dupleReportTest() {

				//given
				CreateReportRequest request = new CreateReportRequest(REASON, REPORT_TARGET_CATEGORY_PRODUCT);

				given(reportRepository.existsByReportTargetIdAndReportTargetCategoryAndReporterId(TARGET_ID, REPORT_TARGET_CATEGORY_PRODUCT, USER_DETAILS_MEMBER_ID)).willReturn(true);

				//when & then
				assertThatThrownBy(() -> reportService.createReport(PRODUCT_ID, USER_DETAILS_MEMBER_ID, request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.DUPLICATE_REPORT.getMessage());
			}

			@Test
			@DisplayName("셀프 신고를 할 수 없습니다")
			public void selfReportTest() {

				//given
				Wifi wifi = WifiFixture.createWifiWithId(WIFI_ID);

				Member seller = MemberFixture.createMember1WithId(SELLER_MEMBER_ID);

				Product product = ProductFixture.createWifiProductWithId(wifi, seller, PRODUCT_ID);

				CreateReportRequest request = new CreateReportRequest(REASON, REPORT_TARGET_CATEGORY_PRODUCT);

				given(reportRepository.existsByReportTargetIdAndReportTargetCategoryAndReporterId(
						product.getId(), REPORT_TARGET_CATEGORY_PRODUCT, seller.getId())).willReturn(false);
				given(memberRepository.findMemberIdByProductId(product.getId()))
						.willReturn(Optional.of(seller.getId()));

				//when & then
				assertThatThrownBy(() -> reportService.createReport(product.getId(), seller.getId(), request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.SELF_REPORT.getMessage());
			}
		}
	}
}
