package com.dapanda.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupResponse {

    private Long id;
    private String message;

    public static SignupResponse from(Long id, String message) {
        return SignupResponse.builder()
                .id(id)
                .message(message)
                .build();
    }
}
