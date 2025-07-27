package com.dapanda.product.repository;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.ReadSellingProductRequest;
import com.dapanda.product.dto.response.*;
import com.dapanda.product.entity.ItemType;
import com.dapanda.product.entity.ProductSortOption;
import com.dapanda.trade.dto.MobileDataScrap;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCustomRepository {

	CursorPageResponse<MobileDataSummary> findMobileDataByCursor(Long cursorId, int size,
			ProductSortOption productSortOption, BigDecimal dataAmount);

	CursorPageResponse<WifiSummary> findWifiByCursor(Long cursorId, int size,
			ProductSortOption productSortOption, boolean isOpen, Double latitude, Double longitude);

	MobileDataInfoResponse findMobileDataInfo(Long productId, Long memberId);

	WifiInfoResponse findWifiInfo(Long productId, Long memberId);

	List<String> findWifiImages(Long wifiId);

	BigDecimal sumSoldMobileDataAmountByMemberId(Long memberId);

	List<ReadSellingProductResponse> findSellingProduct(ReadSellingProductRequest request);

	List<MobileDataScrap> findMobileDataScrap(BigDecimal dataAmount, Long memberId);

	FindMarketPriceResponse findMarketPrice(ItemType itemType);

	Long countSellingProduct(ReadSellingProductRequest request);
}
