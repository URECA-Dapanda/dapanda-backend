package com.dapanda.auth.service;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority(member.getRole().name())))
                .build();
    }

    public UserDetails loadUserByEmailAndProvider(String email, OAuthProvider provider) {

        Member member = memberRepository.findByEmailAndProvider(email, provider)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: email = " + email + ", provider = " + provider));

        return CustomUserDetails.from(member);
    }
}
