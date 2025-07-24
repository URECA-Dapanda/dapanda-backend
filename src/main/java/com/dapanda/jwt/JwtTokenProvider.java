package com.dapanda.jwt;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.auth.service.CustomUserDetailsService;
import com.dapanda.member.entity.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

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
				.claim(JwtClaim.ROLE.getClaim(), member.getRole().name())
				.claim(JwtClaim.ID.getClaim(), member.getId())
				.claim(JwtClaim.PROVIDER.getClaim(), member.getProvider())
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
				.claim(JwtClaim.PROVIDER.getClaim(), member.getProvider().name())
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

	public CustomUserDetails getAuthentication(String email, OAuthProvider provider) {

		return userDetailsService.loadUserByEmailAndProvider(email, provider);
	}

	public String getUserEmailFromToken(String token) {

		return Jwts.parser()
				.setSigningKey(secretKey)
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	public OAuthProvider getProviderFromToken(String token) {

		String raw = getClaims(token).get(JwtClaim.PROVIDER.getClaim(), String.class);

		return OAuthProvider.valueOf(raw);
	}

	public String resolveTokenFromCookie(HttpServletRequest request, String cookieName) {

		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookieName.equals(cookie.getName())) {

					return cookie.getValue();
				}
			}
		}

		return null;
	}

	public int getAccessTokenExpirationSec() {

		return (int) (jwtProperties.getAccessTokenExpiration() / 1000L);
	}

	public int getRefreshTokenExpirationSec() {

		return (int) (jwtProperties.getRefreshTokenExpiration() / 1000L);
	}

}
