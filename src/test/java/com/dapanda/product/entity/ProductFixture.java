package com.dapanda.product.entity;

import static com.dapanda.TestConstants.Wifi.PRODUCT_IMAGE_URL;

import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.product.dto.response.ReadSellingProductResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductFixture {

	public static List<ReadSellingProductResponse> createReadSellingProductResponse() {

		ReadSellingProductResponse response1 = ReadSellingProductResponse.createWifiResponse(
				1L,
				ItemType.MOBILE_DATA,
				ProductState.ACTIVE,
				LocalDateTime.now(),
				LocalDateTime.now(),
				"title1",
				PRODUCT_IMAGE_URL,
				LocalDateTime.now(),
				LocalDateTime.now()
		);

		ReadSellingProductResponse response2 = ReadSellingProductResponse.createWifiResponse(
				2L,
				ItemType.MOBILE_DATA,
				ProductState.ACTIVE,
				LocalDateTime.now(),
				LocalDateTime.now(),
				"title2",
				PRODUCT_IMAGE_URL,
				LocalDateTime.now(),
				LocalDateTime.now()
		);

		return List.of(response1, response2);
	}

	public static Product createProduct1(Member member) {

		return Product.of(
				ProductState.ACTIVE,
				1000,
				1L,
				ItemType.MOBILE_DATA,
				member
		);
	}

	public static Product createWifiProduct(Member member, Wifi wifi) {

		return Product.of(
				ProductState.ACTIVE,
				1000,
				wifi.getId(),
				ItemType.WIFI,
				member
		);
	}

	public static Product createProductWithMobileData(Member member, MobileData mobileData,
			ProductState state) {

		return Product.of(
				state,
				1000,
				mobileData.getId(),
				ItemType.MOBILE_DATA,
				member
		);
	}

	public static List<Product> createProductList(Member member, List<MobileData> mobileDataList,
			ProductState state) {

		List<Product> productList = new ArrayList<>();

		for (MobileData mobileData : mobileDataList) {

			productList.add(createProductWithMobileData(member, mobileData, state));
		}

		return productList;
	}

	public static Product createProduct1WithId(Member member, Long productId) {

		Product product = createProduct1(member);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}

	public static Product createMobileDataProduct(int price, Long mobileDataId, Member member) {

		return Product.of(
				ProductState.ACTIVE,
				price,
				mobileDataId,
				ItemType.MOBILE_DATA,
				member
		);
	}

	public static Product createMobileDataProductInactive(int price, Long mobileDataId,
			Member member) {

		return Product.of(
				ProductState.SOLD_OUT,
				price,
				mobileDataId,
				ItemType.MOBILE_DATA,
				member
		);
	}

	public static Product createMobileDataProductWithId(Long productId, Long mobileDataId,
			int price, Member member) {

		Product product = Product.of(
				ProductState.ACTIVE,
				price,
				mobileDataId,
				ItemType.MOBILE_DATA,
				member
		);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}

	public static Product createMobileDataProductSoldOutWithId(Long productId, Long mobileDataId,
			int price, Member member) {

		Product product = Product.of(
				ProductState.SOLD_OUT,
				price,
				mobileDataId,
				ItemType.MOBILE_DATA,
				member
		);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}

	public static Product createMobileDataProductWithIdWithState(Long productId, Long mobileDataId,
			ProductState state, Member member) {

		Product product = Product.of(
				state,
				1000,
				mobileDataId,
				ItemType.MOBILE_DATA,
				member
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

	public static Product createWifiProductInactive(int price, Long wifiId, Member member) {

		return Product.of(
				ProductState.SOLD_OUT,
				price,
				wifiId,
				ItemType.WIFI,
				member
		);
	}

	public static Product createWifiProductWithId(Wifi wifi, Member seller, Long productId) {

		Product product = Product.of(ProductState.ACTIVE, 1000, wifi.getId(), ItemType.WIFI,
				seller);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}

	public static Product createWifiProductWithId(Long productId, Long wifiId, Long memberId,
			int price) {

		Product product = Product.of(
				ProductState.ACTIVE,
				price,
				wifiId,
				ItemType.WIFI,
				MemberFixture.createMember1WithId(memberId)
		);

		ReflectionTestUtils.setField(product, "id", productId);

		return product;
	}
}
