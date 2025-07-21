package com.dapanda.review.repository;

import com.dapanda.member.entity.Member;
import com.dapanda.review.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

	List<Review> findByTradeMember(Member member);

}
