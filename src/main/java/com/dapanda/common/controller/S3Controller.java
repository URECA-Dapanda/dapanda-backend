package com.dapanda.common.controller;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.dto.request.PreSignRequest;
import com.dapanda.common.dto.response.PreSignedFileResponse;
import com.dapanda.common.exception.CommonResponse;
import jakarta.validation.Valid;
import java.net.URL;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Profile("!test")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class S3Controller {

	private final AmazonS3 amazonS3;
	private final String bucket = "dpd-bucket";

	@PostMapping("/images/presign")
	public CommonResponse<List<PreSignedFileResponse>> getPresignedUrl(
			@RequestBody @Valid PreSignRequest request,
			@AuthenticationPrincipal CustomUserDetails customUserDetails) {

		Long userId = customUserDetails.getId();
		List<PreSignedFileResponse> results = new ArrayList<>();

		for (String originalFilename : request.filenames()) {
			// 1. 확장자 추출
			String ext = "";
			int dotIdx = originalFilename.lastIndexOf('.');
			if (dotIdx != -1) {
				ext = originalFilename.substring(dotIdx);
			}
			// 2. 무작위 UUID
			String uuid = UUID.randomUUID().toString();
			// 3. S3 Key
			String key = "images/" + userId + "/" + uuid + ext;
			// 4. Presigned URL
			Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 5);
			GeneratePresignedUrlRequest presignedUrlRequest =
					new GeneratePresignedUrlRequest(bucket, key)
							.withMethod(HttpMethod.PUT)
							.withExpiration(expiration);
			URL url = amazonS3.generatePresignedUrl(presignedUrlRequest);

			results.add(new PreSignedFileResponse(
					originalFilename,
					url.toString(),
					"https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + key,
					key
			));
		}

		return CommonResponse.success(results);
	}

}
