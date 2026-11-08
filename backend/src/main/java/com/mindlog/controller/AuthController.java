package com.mindlog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindlog.dto.ErrorResponse;
import com.mindlog.dto.LoginRequest;
import com.mindlog.dto.RegisterRequest;
import com.mindlog.dto.UserResponse;
import com.mindlog.security.LoginUser;
import com.mindlog.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * 認証API（API設計書 4節）。ログアウト（API-03）は SecurityConfig で設定している。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final String LOGIN_FAILED = "メールアドレスまたはパスワードが正しくありません。";

	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
			.getContextHolderStrategy();

	public AuthController(UserService userService, AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
	}

	/** API-01 ユーザー登録。登録してもログイン状態にはしない */
	@PostMapping("/register")
	public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
		userService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/** API-02 ログイン */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken
					.unauthenticated(nullToEmpty(request.email()), nullToEmpty(request.password())));
		} catch (AuthenticationException e) {
			// メールアドレスが無い場合もパスワード誤りの場合も同じ応答にする（NFR-002）
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.of(LOGIN_FAILED));
		}

		// ログイン前のセッションIDを使い回させない（セッション固定攻撃の対策）
		if (httpRequest.getSession(false) != null) {
			httpRequest.changeSessionId();
		}

		// ログイン状態をセッションに保存する。次のリクエストからは Cookie（JSESSIONID）で判別される
		SecurityContext context = securityContextHolderStrategy.createEmptyContext();
		context.setAuthentication(authentication);
		securityContextHolderStrategy.setContext(context);
		securityContextRepository.saveContext(context, httpRequest, httpResponse);

		return ResponseEntity.ok(UserResponse.from((LoginUser) authentication.getPrincipal()));
	}

	/** API-04 ログインユーザー取得（未ログインの場合は SecurityConfig で 401 になる） */
	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal LoginUser loginUser) {
		return UserResponse.from(loginUser);
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
