package com.dapanda.report.repository;

import com.dapanda.member.entity.Member;
import com.dapanda.report.entity.Report;
import com.dapanda.report.entity.ReportTargetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

	boolean existsByReportTargetIdAndReportTargetCategoryAndReporterId(Long targetId, ReportTargetCategory category, Long memberId);
}
