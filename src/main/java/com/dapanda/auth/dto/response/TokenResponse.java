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
public class TokenResponse {

    private String accessToken;

    public static TokenResponse from(String accessToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
