package com.dapanda.product.repository;

import com.dapanda.product.entity.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductCustomRepository {

	// Lock 걸고 상품 조회
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Product p WHERE p.id = :id")
	Optional<Product> findByIdForUpdate(@Param("id") Long id);

	boolean existsByIdAndMember_Id(Long id, Long memberId);

	@Query("SELECT p FROM Product p WHERE p.createdAt < :CURRENT_TIMESTAMP AND p.state = 'ACTIVE'")
	List<Product> findAllBeforeThisMonthAndIsActive();
}
