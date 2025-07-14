package com.dapanda.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummary {

	private Long id;
	private int price;
	private Long itemId;
	private String memberName;
}
