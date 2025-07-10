package com.dapanda.auth.info;

import com.dapanda.auth.entity.OAuthProvider;
import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(OAuthProvider provider,
            Map<String, Object> attributes) {

        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            case NAVER -> new NaverOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("Invalid Provider Type: " + provider);
        };
    }

}
