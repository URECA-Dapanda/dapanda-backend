package com.dapanda.plan.repository;

import com.dapanda.member.entity.Member;
import com.dapanda.plan.entity.Plan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

	boolean existsByMember(Member member);

	Optional<Plan> findByMember(Member member);

	Optional<Plan> findByMemberId(Long memberId);

}
