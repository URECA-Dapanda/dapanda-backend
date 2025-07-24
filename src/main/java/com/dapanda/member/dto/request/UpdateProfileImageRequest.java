package com.dapanda.member.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record UpdateProfileImageRequest(

		@NotEmpty(message = "filename은 필수입니다.")
		String imageUrl
) {

}
