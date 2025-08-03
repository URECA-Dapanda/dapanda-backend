package com.dapanda.product.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.dapanda.product.entity.ProductState;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WifiInfoResponse {

	private Long productId;
	private ProductState productState;
	private Long itemId;
	private int price;
	private Long memberId;
	private String memberName;
	private String profileImageUrl;
	private String title;
	private String content;
	private double latitude;
	private double longitude;
	private String address;
	private float averageRate;
	private int reviewCount;
	private boolean myProduct;
	private List<String> imageUrls;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private boolean open;
	private LocalDateTime updatedAt;

	public WifiInfoResponse withImageUrls(List<String> imageUrls) {

		return this.toBuilder()
				.imageUrls(imageUrls)
				.build();
	}
}
