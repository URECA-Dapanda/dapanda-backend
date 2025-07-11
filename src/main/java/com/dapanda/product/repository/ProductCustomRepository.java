package com.dapanda.product.repository;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.product.entity.ItemType;
import com.dapanda.product.entity.ProductSortOption;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCustomRepository {

	public CursorPageResponse<?> findProductsByCursor(ItemType itemType, Long cursorId, int size,
			ProductSortOption productSortOption, Integer dataAmount, Double latitude,
			Double longitude);
}
