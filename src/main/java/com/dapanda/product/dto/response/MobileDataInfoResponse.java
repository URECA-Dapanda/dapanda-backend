package com.dapanda.product.dto.response;

import com.dapanda.member.entity.Member;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MobileDataInfoResponse {

	private Long productId;
	private Long itemId;
	private int price;
	private Member member;
	private float remainAmount;
	private int pricePer100MB;
	private double averageRate;
	private int reviewCount;
	private LocalDateTime updatedAt;

	public static MobileDataInfoResponse of(Long productId, Long itemId, int price, Member member,
			float remainAmount, int pricePer100MB, double averageRate, int reviewCount,
			LocalDateTime updatedAt) {

		return MobileDataInfoResponse.builder()
				.productId(productId)
				.itemId(itemId)
				.price(price)
				.member(member)
				.remainAmount(remainAmount)
				.pricePer100MB(pricePer100MB)
				.averageRate(averageRate)
				.reviewCount(reviewCount)
				.updatedAt(updatedAt)
				.build();
	}
}
