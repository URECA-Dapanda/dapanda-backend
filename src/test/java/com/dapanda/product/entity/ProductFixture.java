package com.dapanda.product.entity;

import com.dapanda.member.entity.MemberFixture;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductFixture {

	public static Product createMobileDataProduct1(Long productId, int price, Long mobileDataId,
			Long memberId) {

		Product product = Product.of(
				ProductState.ACTIVE,
				price,
				mobileDataId,
				ItemType.MOBILE_DATA,
				MemberFixture.createMember1(memberId)
		);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}

	public static Product createMobileDataProduct2(Long productId, Long mobileDataId) {

		Product product = Product.of(
				ProductState.ACTIVE,
				4000,
				mobileDataId,
				ItemType.MOBILE_DATA,
				MemberFixture.createMember1(1L)
		);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}

	public final Product createWifiProduct1(Long productId, int price, Long wifiId, Long memberId) {

		Product product = Product.of(
				ProductState.ACTIVE,
				price,
				wifiId,
				ItemType.WIFI,
				MemberFixture.createMember1(memberId)
		);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}

	public final Product createWifiProduct2(Long productId, Long wifiId) {

		Product product = Product.of(
				ProductState.ACTIVE,
				1000,
				wifiId,
				ItemType.WIFI,
				MemberFixture.createMember2(2L)
		);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}
}