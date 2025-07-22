package com.dapanda.product.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WifiInfoResponse {

	private Long productId;
	private Long itemId;
	private int price;
	private Long memberId;
	private String memberName;
	private String title;
	private String content;
	private double latitude;
	private double longitude;
	private double averageRate;
	private int reviewCount;
	private List<String> imageUrls;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private LocalDateTime updatedAt;

	public WifiInfoResponse withImageUrls(List<String> imageUrls) {

		return WifiInfoResponse.builder()
				.imageUrls(imageUrls)
				.build();
	}
}
