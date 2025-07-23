package com.dapanda.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {

		log.info("request.getPrincipal() = {}", request.getPrincipal());
		log.info("(Principal) attributes.get(\"SPRING_SECURITY_PRINCIPAL\") = {}", attributes.get("SPRING_SECURITY_PRINCIPAL"));

		return (Principal) attributes.get("SPRING_SECURITY_PRINCIPAL");
	}
}
