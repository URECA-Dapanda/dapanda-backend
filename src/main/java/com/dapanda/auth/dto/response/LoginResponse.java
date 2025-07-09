package com.dapanda.auth.dto.response;

import jakarta.validation.constraints.NotBlank;

public record LoginResponse(
        @NotBlank
        String name,

        @NotBlank
        String message
) {

}
