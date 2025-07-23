package com.dapanda.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PreSignRequest(

		@NotEmpty(message = "filename은 필수입니다.")
		List<@NotBlank String> filenames
) {

}
