package com.dapanda.plan.repository;

import com.dapanda.member.entity.Member;
import com.dapanda.plan.entity.Plan;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface PlanRepository extends JpaRepository<Plan, Long> {

	boolean existsByMember(Member member);

	Optional<Plan> findByMember(Member member);

	Optional<Plan> findByMemberId(Long memberId);

	@Modifying
	@Query("UPDATE Plan p SET p.currentDataAmount = p.providingDataAmount")
	void resetMemberMobileDataPlan();
}
