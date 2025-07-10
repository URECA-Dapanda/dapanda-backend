package com.dapanda.refreshToken.repository;

import com.dapanda.member.entity.Member;
import com.dapanda.refreshToken.entity.RefreshToken;
import com.dapanda.refreshToken.entity.TokenState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByMember(Member member);

	Optional<RefreshToken> findByMemberAndState(Member member, TokenState state);

}
