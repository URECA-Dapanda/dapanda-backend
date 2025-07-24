package com.dapanda.common.service;

import com.dapanda.common.entity.ImageExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3Service {

	public boolean isNotSupportedImageExtension(String imageUrl) {

		return ImageExtension.isValid(imageUrl);
	}

}
