package com.dapanda.product.repository;

import com.dapanda.product.entity.MobileData;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface MobileDataRepository extends JpaRepository<MobileData, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM MobileData m WHERE m.id = :id")
	@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
	Optional<MobileData> findByIdForUpdate(Long id);
}
