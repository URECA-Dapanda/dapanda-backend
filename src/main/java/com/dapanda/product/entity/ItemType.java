package com.dapanda.product.entity;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;

public enum ItemType {

	HOTSPOT,
	WIFI,
	MOBILE_DATA,
	;

	public static ItemType from(String name) {

		try {

			return ItemType.valueOf(name);
		} catch (IllegalArgumentException e) {

			throw new GlobalException(ResultCode.INVALID_ITEM_TYPE);
		}
	}
}
