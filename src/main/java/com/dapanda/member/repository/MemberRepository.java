package com.dapanda.member.repository;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

	Optional<Member> findByEmail(String email);

	boolean existsByEmail(String email);

	Optional<Member> findByEmailAndProvider(String email, OAuthProvider provider);

	boolean existsByName(String name);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM Member m WHERE m.id = :id")
	Optional<Member> findByIdForUpdate(Long id);
}
