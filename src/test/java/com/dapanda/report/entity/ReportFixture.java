package com.dapanda.report.entity;

import com.dapanda.member.entity.Member;
import com.dapanda.product.entity.Product;
import org.springframework.test.util.ReflectionTestUtils;

public class ReportFixture {

	public static Report createReportFromProduct1(Product product, ReportTargetCategory category, Member reporter) {

		return Report.of(
				"너무 비싸게 올렸어요",
				product.getId(),
				category,
				reporter
		);
	}

	public static Report createReportFromProductWithId(Product product, ReportTargetCategory category, Member reporter, Long reportId) {

		Report report = Report.of("너무 비싸게 올렸어요", product.getId(), category, reporter);

		ReflectionTestUtils.setField(report, "id", reportId);

		return report;
	}
}
