package com.mindlog.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security の設定（API設計書 1.2〜1.5節）。
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository)
			throws Exception {
		http
				// CSRF対策：トークンを Cookie（XSRF-TOKEN）で渡し、React はヘッダー（X-XSRF-TOKEN）で送り返す（1.3節）
				.csrf(csrf -> csrf.spa())
				// ログイン状態はセッションに保存する（1.2節）
				.securityContext(context -> context.securityContextRepository(securityContextRepository))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
						.requestMatchers("/api/**").authenticated()
						// React の画面（静的ファイル）は誰でも取得できる
						.anyRequest().permitAll())
				.exceptionHandling(exception -> exception
						// 未ログインで API を呼んだ場合（401）
						.authenticationEntryPoint((request, response, e) -> writeError(response,
								HttpStatus.UNAUTHORIZED, "ログインしてください。"))
						// CSRFトークンが無い・不正な場合など（403）
						.accessDeniedHandler((request, response, e) -> writeError(response,
								HttpStatus.FORBIDDEN, "不正なリクエストです。画面を再読み込みしてください。")))
				// ログアウト：セッションを破棄して 204 を返す（API-03）
				.logout(logout -> logout
						.logoutUrl("/api/auth/logout")
						.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
				// ログインは AuthController の JSON API で行うため、標準のログイン画面・Basic認証は使わない
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable);
		return http.build();
	}

	/** パスワードのハッシュ化方式：BCrypt（DB設計書 7節 No.6） */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	/** エラーレスポンス（API設計書 2.2節）を書き出す。メッセージは固定文言のみのため手書きの JSON で十分 */
	private static void writeError(HttpServletResponse response, HttpStatus status, String message)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"message\":\"" + message + "\",\"errors\":[]}");
	}
}
