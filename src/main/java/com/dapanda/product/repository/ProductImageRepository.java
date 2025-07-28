package com.dapanda.product.repository;

import com.dapanda.product.entity.ProductImage;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

	@Modifying
	@Query("DELETE FROM ProductImage pi WHERE pi.wifiId = :wifiId")
	void removeProductImagesById(@Param("wifiId") Long id);
}
