package com.dapanda.auth.service;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.auth.info.OAuth2UserInfo;
import com.dapanda.auth.info.OAuth2UserInfoFactory;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberRole;
import com.dapanda.member.repository.MemberRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String providerStr = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.valueOf(providerStr.toUpperCase());

        log.info("OAuth2 로그인 시도: provider = {}", provider);
        log.info("attributes = {}", oAuth2User.getAttributes());

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider,
                oAuth2User.getAttributes());
        String email = userInfo.getEmail();
        log.info("email = {}", email);

        if (email == null) {
            throw new OAuth2AuthenticationException(
                    provider + " 계정에 이메일 정보가 없습니다. 이메일 제공 동의가 필요합니다.");
        }

        Member member = memberRepository.findByEmailAndProvider(email, provider)
                .orElseGet(() -> registerUser(userInfo, provider));

        Map<String, Object> attributes = new HashMap<>(userInfo.getAttributes());
        attributes.put("email", email);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes,
                "email"
        );
    }


    private Member registerUser(OAuth2UserInfo userInfo, OAuthProvider provider) {

        Member member = Member.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .provider(provider)
                .role(MemberRole.ROLE_MEMBER)
                .build();

        return memberRepository.save(member);
    }
}
