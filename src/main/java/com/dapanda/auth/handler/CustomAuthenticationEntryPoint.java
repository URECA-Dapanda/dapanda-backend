package com.dapanda.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

		String origin = request.getHeader("Origin");
		String host = request.getHeader("Host");
		boolean isLocal = (origin != null && origin.contains("localhost")) ||
				(host != null && host.contains("localhost"));

		String redirectUrl;
		if (isLocal) {
			redirectUrl = "http://localhost:3000/error";
		} else {
			redirectUrl = "https://dapanda.org/error";
		}

		response.sendRedirect(redirectUrl);
	}
}
