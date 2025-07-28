package com.dapanda.member.repository;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.member.entity.Member;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

	Optional<Member> findByEmail(String email);

	boolean existsByEmail(String email);

	Optional<Member> findByEmailAndProvider(String email, OAuthProvider provider);

	boolean existsByName(String name);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM Member m WHERE m.id = :id")
	@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
	Optional<Member> findByIdForUpdate(Long id);

	@Modifying
	@Query("UPDATE Member m SET m.buyingData = 0, m.sellingData = 0")
	void resetAllDataAmounts();
}
