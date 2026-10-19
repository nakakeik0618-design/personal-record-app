package com.mindlog.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mindlog.entity.User;

/**
 * ログイン中のユーザーを表す、Spring Security 用のクラス。
 * セッションに保存され、コントローラーでは @AuthenticationPrincipal で受け取れる。
 */
public class LoginUser implements UserDetails {

	private final Long id;
	private final String email;
	private final String passwordHash;

	public LoginUser(User user) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.passwordHash = user.getPasswordHash();
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	/** 権限（管理者など）は使わないため空 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}
}
