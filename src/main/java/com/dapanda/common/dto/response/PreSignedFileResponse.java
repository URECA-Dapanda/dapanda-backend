package com.dapanda.common.dto.response;

public record PreSignedFileResponse(
		String filename,
		String url,
		String publicUrl,
		String key
) {

}
