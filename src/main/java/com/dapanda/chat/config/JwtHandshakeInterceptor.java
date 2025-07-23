package com.dapanda.chat.config;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.jwt.JwtClaim;
import com.dapanda.jwt.JwtPrinciple;
import com.dapanda.member.entity.Member;
import com.dapanda.member.service.MemberService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	@Value("${jwt.secret}")
	private String secretKey;

	private final MemberService memberService;

	private Key getSigningKey() {
		byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

		log.info("beforeHandshake 로그");

		if (request instanceof ServletServerHttpRequest) {

			HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

			Cookie[] cookies = servletRequest.getCookies();

			if (cookies != null) {
				Optional<Cookie> jwtCookie = Arrays.stream(cookies)
						.filter(cookie -> JwtPrinciple.ACCESS_TOKEN.getKey().equals(cookie.getName()))
						.findFirst();

				if (jwtCookie.isPresent()) {
					String accessToken = jwtCookie.get().getValue();
					log.info("Handshake - accessToken from cookie: {}", accessToken);

					try {
						Jws<Claims> claimsJws = Jwts.parserBuilder()
								.setSigningKey(getSigningKey())
								.build()
								.parseClaimsJws(accessToken);

						Claims claims = claimsJws.getBody();

						Long memberId = claims.get(JwtClaim.ID.getClaim(), Long.class);

						Member member = memberService.findById(memberId);

						// Principal 생성 및 attributes에 저장 -> 웹소켓 세션에서 Principal로 사용 가능
						CustomUserDetails userDetails = CustomUserDetails.from(member); // member 객체로 CustomUserDetails 생성

						UsernamePasswordAuthenticationToken authentication =
								new UsernamePasswordAuthenticationToken(
										userDetails,
										null,
										userDetails.getAuthorities()
								);

						attributes.put("SPRING_SECURITY_PRINCIPAL", authentication);

						log.info("Handshake - AccessToken authenticated. Member ID: {}", memberId);
						return true; // 핸드셰이크 계속 진행
					} catch (ExpiredJwtException e) {
						log.warn("Handshake - JWT expired: {}", e.getMessage());
					} catch (SignatureException e) {
						log.warn("Handshake - Invalid JWT signature: {}", e.getMessage());
					} catch (Exception e) {
						log.warn("Handshake - Failed to authenticate JWT: {}", e.getMessage());
					}
				} else {
					log.warn("Handshake - JWT cookie not found.");
				}
			} else {
				log.warn("Handshake - No cookies in HTTP request.");
			}
		} else {
			log.warn("Handshake - Not a ServletServerHttpRequest.");
		}
		return false; // 인증 실패 시 핸드셰이크 거부
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

		log.info("afterHandshake 로그");
	}
}
