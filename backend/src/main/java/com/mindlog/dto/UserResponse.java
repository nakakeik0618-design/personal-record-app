package com.mindlog.dto;

import com.mindlog.security.LoginUser;

/**
 * ログイン中のユーザー情報（API-02, API-04）。
 */
public record UserResponse(Long id, String email) {

	public static UserResponse from(LoginUser user) {
		return new UserResponse(user.getId(), user.getEmail());
	}
}
