package com.dapanda.product.service;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.MobileDataCursorRequest;
import com.dapanda.product.dto.request.WifiCursorRequest;
import com.dapanda.product.dto.response.MobileDataInfoResponse;
import com.dapanda.product.entity.ProductSortOption;
import com.dapanda.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	public CursorPageResponse<MobileDataSummary> findMobileDataByCursor(
			MobileDataCursorRequest request) {

		return productRepository.findMobileDataByCursor(request.getCursorId(), request.getSize(),
				ProductSortOption.from(request.getProductSortOption()), request.getDataAmount());
	}

	public CursorPageResponse<WifiSummary> findWifiByCursor(WifiCursorRequest request) {

		return productRepository.findWifiByCursor(request.getCursorId(), request.getSize(),
				ProductSortOption.from(request.getProductSortOption()), request.isOpen(),
				request.getLatitude(), request.getLongitude());
	}

	public MobileDataInfoResponse findMobileDataInfo(Long productId) {

		MobileDataInfoResponse response = productRepository.findMobileDataInfo(productId);

		if (!productRepository.existsById(productId)) {
			throw new GlobalException(ResultCode.NOT_EXIST_PRODUCT);
		}
		if (response == null) {
			throw new GlobalException(ResultCode.INVALID_PRODUCT);
		}

		return response;
	}


}
