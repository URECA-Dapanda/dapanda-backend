package com.dapanda.fcm_token.repository;

import com.dapanda.fcm_token.entity.FcmToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

	Optional<FcmToken> findByMemberId(Long memberId);
}
