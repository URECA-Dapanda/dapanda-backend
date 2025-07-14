package com.dapanda.product.entity;

import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductFixture {

	public static Product createMobileDataProduct(int price, Long mobileDataId, Member member) {

		return Product.of(
				ProductState.ACTIVE,
				price,
				mobileDataId,
				ItemType.MOBILE_DATA,
				member
		);
	}

	public static Product createMobileDataProductWithId(Long productId, Long mobileDataId) {

		Product product = Product.of(
				ProductState.ACTIVE,
				4000,
				mobileDataId,
				ItemType.MOBILE_DATA,
				MemberFixture.createMember1()
		);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}

	public static Product createWifiProduct(int price, Long wifiId, Member member) {

		return Product.of(
				ProductState.ACTIVE,
				price,
				wifiId,
				ItemType.WIFI,
				member
		);
	}

	public static Product createWifiProductWithId(Long productId, Long wifiId) {

		Product product = Product.of(
				ProductState.ACTIVE,
				1000,
				wifiId,
				ItemType.WIFI,
				MemberFixture.createMember1()
		);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}
}