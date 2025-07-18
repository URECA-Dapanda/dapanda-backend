package com.dapanda.report.entity;

import com.dapanda.common.entity.CreatedAtEntity;
import com.dapanda.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String reason;

	private Long reportTargetId;

	@Enumerated(EnumType.STRING)
	private ReportTargetCategory reportTargetCategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporter_id")
	private Member reporter;

	public static Report of(String reason, Long reportTargetId, ReportTargetCategory reportTargetCategory, Member reporter) {

		return Report.builder()
				.reason(reason)
				.reportTargetId(reportTargetId)
				.reportTargetCategory(reportTargetCategory)
				.reporter(reporter)
				.build();
	}
}
