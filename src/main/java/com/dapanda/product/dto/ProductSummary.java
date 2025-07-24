package com.dapanda.product.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummary {

	private Long id;
	private int price;
	private Long itemId;
	private String memberName;
	private String profileImageUrl;
}
