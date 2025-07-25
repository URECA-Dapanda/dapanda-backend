package com.dapanda.product.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MobileData {

	public static final float MAX_TRANSFERABLE_DATA_AMOUNT = 2.0F; // 데이터 전송 제한 정책

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private BigDecimal dataAmount;
	private BigDecimal remainAmount;
	private int pricePer100MB;
	private boolean isSplitType;

	public static MobileData of(BigDecimal dataAmount, BigDecimal remainAmount, int pricePer100MB,
			boolean isSplitType) {

		return MobileData.builder()
				.dataAmount(dataAmount)
				.remainAmount(remainAmount)
				.pricePer100MB(pricePer100MB)
				.isSplitType(isSplitType)
				.build();
	}

	public static MobileData singleOf(BigDecimal dataAmount, int totalPrice, boolean isSplitType) {

		int pricePer100MB = new BigDecimal(totalPrice)
				.divide(dataAmount.multiply(BigDecimal.TEN), 0, java.math.RoundingMode.CEILING)
				.intValue();

		return MobileData.builder()
				.dataAmount(dataAmount)
				.remainAmount(dataAmount)
				.pricePer100MB(pricePer100MB)
				.isSplitType(isSplitType)
				.build();
	}

	public void updateMobileData(BigDecimal changedAmount, int pricePer100MB, boolean isSplitType) {

		this.dataAmount = this.dataAmount.add(changedAmount);
		this.remainAmount = this.remainAmount.add(changedAmount);
		this.pricePer100MB = pricePer100MB;
		this.isSplitType = isSplitType;
	}

	public void deductRemainAmount(BigDecimal dataAmount) {

		this.remainAmount = this.remainAmount.subtract(dataAmount);
	}

	public void update100MBPerPrice(int price, BigDecimal remainAmount) {

		BigDecimal pricePer100MB = BigDecimal.valueOf(price)
				.divide(remainAmount.multiply(BigDecimal.TEN), RoundingMode.CEILING);

		this.pricePer100MB = pricePer100MB.intValue();
	}
}
