package com.dapanda.fcmToken.repository;

import com.dapanda.fcmToken.entity.FcmToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

	Optional<FcmToken> findByMemberId(Long memberId);
}
