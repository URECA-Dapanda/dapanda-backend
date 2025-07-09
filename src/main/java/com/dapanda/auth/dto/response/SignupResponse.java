package com.dapanda.auth.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignupResponse(
        @NotNull
        Long id,

        @NotBlank
        String message
) {

}
