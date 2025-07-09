package com.dapanda.auth.dto.response;

import jakarta.validation.constraints.NotBlank;

public record TokenResponse(
        @NotBlank
        String accessToken
) {

}
