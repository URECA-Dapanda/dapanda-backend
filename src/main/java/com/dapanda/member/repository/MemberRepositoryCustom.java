package com.dapanda.member.repository;

import java.util.Optional;

public interface MemberRepositoryCustom {

	Optional<Long> findMemberIdByProductId(Long productId);

	Optional<Long> findMemberIdByReviewId(Long reviewId);
}
