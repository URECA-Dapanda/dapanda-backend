package com.dapanda.product.entity;

import com.dapanda.common.entity.BaseEntity;
import com.dapanda.member.entity.Member;
import com.dapanda.product.repository.WifiRepository;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private ProductState state;

	private int price;

	private Long itemId;

	@Enumerated(EnumType.STRING)
	private ItemType itemType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Member member;

	public Wifi getWifiFromRepository(WifiRepository wifiRepository) {
		if (this.getItemType() == ItemType.WIFI && this.getItemId() != null) {
			return wifiRepository.findById(this.getItemId()).orElse(null);
		}
		return null;
	}

	public static Product of(ProductState state, int price, Long itemId, ItemType itemType,
			Member member) {

		return Product.builder()
				.state(state)
				.price(price)
				.itemId(itemId)
				.itemType(itemType)
				.member(member)
				.build();
	}

	public void updatePrice(int price) {

		this.price = price;
	}

	public void changeState(ProductState state) {

		this.state = state;
	}
}
