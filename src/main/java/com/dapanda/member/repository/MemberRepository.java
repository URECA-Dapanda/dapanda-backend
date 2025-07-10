package com.dapanda.member.repository;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByEmail(String email);

	boolean existsByEmail(String email);

	Optional<Member> findByEmailAndProvider(String email, OAuthProvider provider);

	boolean existsByName(String name);
}
