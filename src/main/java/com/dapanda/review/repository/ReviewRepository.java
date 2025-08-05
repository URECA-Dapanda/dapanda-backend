package com.dapanda.review.repository;

import com.dapanda.review.entity.Review;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

	@Query("""
			SELECT m.id
			FROM Review r
			JOIN r.trade t
			JOIN t.member m
			WHERE r.id = :reviewId
			""")
	Optional<Long> findMemberIdByReviewId(Long reviewId);
}
