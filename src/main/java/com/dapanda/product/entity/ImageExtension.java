package com.dapanda.product.entity;

import java.util.Arrays;

public enum ImageExtension {
	JPG(".jpg"),
	JPEG(".jpeg"),
	PNG(".png");

	private final String extension;

	ImageExtension(String extension) {
		this.extension = extension;
	}

	public static boolean isValid(String fileName) {
		if (fileName == null) {
			return false;
		}
		String lowered = fileName.toLowerCase();
		return Arrays.stream(values())
				.anyMatch(ext -> lowered.endsWith(ext.getExtension()));
	}

	public String getExtension() {
		return extension;
	}
}
