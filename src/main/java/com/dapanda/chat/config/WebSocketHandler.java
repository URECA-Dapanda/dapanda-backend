package com.dapanda.chat.config;

import com.dapanda.jwt.JwtPrinciple;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
public class WebSocketHandler implements ChannelInterceptor {

	@Value("${jwt.secret}")
	private String secretKey;

	private Key getSigningKey() {

		byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

		return Keys.hmacShaKeyFor(keyBytes);
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.CONNECT.equals(accessor.getCommand())){

			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

			if (attributes != null){

				HttpServletRequest request = attributes.getRequest();
				Cookie[] cookies = request.getCookies();

				if (cookies != null){

					Optional<Cookie> jwtCookie = Arrays.stream(cookies)
							.filter(cookie -> JwtPrinciple.ACCESS_TOKEN.getKey().equals(cookie.getName()))
							.findFirst();

					if (jwtCookie.isPresent()){

						String accessToken = jwtCookie.get().getValue();

						log.info("accessToken : {}", accessToken);

						try{
							Jwts.parserBuilder()
									.setSigningKey(getSigningKey())
									.build()
									.parseClaimsJws(accessToken);

							log.info("accessToken 인증 완료");
						}catch (Exception e){

							log.warn("토큰이 유효하지 않습니다. : {}", e.getMessage());
						}

					}else log.warn("쿠키를 찾을 수 없습니다.");

				}else log.warn("HTTP 요청에 쿠키가 없습니다.");

			}else log.warn("RequestContextHolder 에서 RequestAttributes 를 찾을 수 없습니다.");
		}

		return message;
	}
}
