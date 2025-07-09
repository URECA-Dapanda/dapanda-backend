package com.dapanda.jwt;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.auth.service.CustomUserDetailsService;
import com.dapanda.member.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService userDetailsService;
    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generateAccessToken(Member member) {

        return Jwts.builder()
                .setSubject(member.getEmail())
                .claim("role", member.getRole().name())
                .claim("id", member.getId())
                .claim("provider", member.getProvider())
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Member member) {

        log.info("refreshToken 발급용 provider: {}", member.getProvider());

        return Jwts.builder()
                .setSubject(member.getEmail())
                .claim("provider", member.getProvider().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("잘못된 JWT 토큰입니다: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("JWT 서명이 올바르지 않습니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("JWT 관련 예외 발생: {}", e.getMessage());
        }

        return false;
    }

    public Claims getClaims(String token) {

        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token)
                .getBody();
    }

    public String resolveToken(HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {

            return bearer.substring(7);
        }

        return null;
    }

    public Authentication getAuthentication(String email, OAuthProvider provider) {

        UserDetails userDetails = userDetailsService.loadUserByEmailAndProvider(email, provider);

        return new UsernamePasswordAuthenticationToken(userDetails, "",
                userDetails.getAuthorities());
    }


    public String getUserEmailFromToken(String token) {

        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public OAuthProvider getProviderFromToken(String token) {

        String raw = getClaims(token).get("provider", String.class);

        return OAuthProvider.valueOf(raw);
    }

}
