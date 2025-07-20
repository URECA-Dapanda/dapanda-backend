package com.dapanda.common.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.dapanda.auth.handler.OAuth2FailureHandler;
import com.dapanda.auth.handler.OAuth2SuccessHandler;
import com.dapanda.auth.service.CustomOAuth2UserService;
import com.dapanda.auth.service.CustomUserDetailsService;
import com.dapanda.jwt.JwtAuthenticationFilter;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.service.MemberService;
import com.dapanda.refreshToken.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final OAuth2FailureHandler oAuth2FailureHandler;
	private final CustomUserDetailsService userDetailsService;

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter(
			JwtTokenProvider jwtTokenProvider,
			MemberService memberService,
			RefreshTokenService refreshTokenService
	) {
		return new JwtAuthenticationFilter(jwtTokenProvider, memberService, refreshTokenService);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

		http
				.cors(withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.headers(headers -> headers.frameOptions(
						FrameOptionsConfig::disable
				))
				.sessionManagement(
						sess -> sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/api/**", "/api-docs.html", "/docs/**",
								"/oauth2/**", "error", "/actuator/health", "/default-ui.css",
								"/api/auth/**", "/connect/**").permitAll()
						.anyRequest().authenticated()
				)
				.oauth2Login(oauth2 -> oauth2
						.userInfoEndpoint(userInfo -> userInfo
								.userService(customOAuth2UserService)
						)
						.successHandler(oAuth2SuccessHandler)
						.failureHandler(oAuth2FailureHandler)
				);

		http.addFilterAfter(
				jwtAuthenticationFilter,
				OAuth2AuthorizationRequestRedirectFilter.class
		);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
			throws Exception {

		return configuration.getAuthenticationManager();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());

		return provider;
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {

		return new WebMvcConfigurer() {

			@Override
			public void addCorsMappings(CorsRegistry registry) {

				registry.addMapping("/api/**")
						.allowedOrigins("http://localhost:3000", "https://dapanda.org",
								"https://www.dapanda.org")
						.allowedMethods("*")
						.allowCredentials(true);
			}
		};
	}

}
