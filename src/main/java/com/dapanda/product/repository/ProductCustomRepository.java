package com.dapanda.product.repository;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.response.MobileDataInfoResponse;
import com.dapanda.product.dto.response.WifiInfoResponse;
import com.dapanda.product.entity.ProductSortOption;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCustomRepository {

	public CursorPageResponse<MobileDataSummary> findMobileDataByCursor(Long cursorId, int size,
			ProductSortOption productSortOption, Float dataAmount);

	public CursorPageResponse<WifiSummary> findWifiByCursor(Long cursorId, int size,
			ProductSortOption productSortOption, boolean isOpen, Double latitude, Double longitude);

	public MobileDataInfoResponse findMobileDataInfo(Long productId);

	public WifiInfoResponse findWifiInfo(Long productId);
}
