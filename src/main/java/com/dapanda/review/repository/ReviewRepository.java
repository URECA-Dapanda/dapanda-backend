package com.dapanda.review.repository;

import com.dapanda.member.entity.Member;
import com.dapanda.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

	@Query("select count(r), avg(r.rating) from Review r where r.trade.member = :member")
	Object[] findReviewStatsByMember(@Param("member") Member member);
}
