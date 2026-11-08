package com.mindlog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * ユーザー登録のリクエスト（API-01）。
 */
public record RegisterRequest(

		/** 「@」と「.」を含む標準的な形式（FR-004） */
		@NotNull(message = "メールアドレスを入力してください。")
		@Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "メールアドレスの形式が正しくありません。")
		@Size(max = 255, message = "メールアドレスは255文字以内で入力してください。")
		String email,

		/** 8文字以上、英字と数字を両方含む（FR-002）。BCrypt の制限により72文字まで */
		@NotNull(message = "パスワードを入力してください。")
		@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$", message = "パスワードは8文字以上で、英字と数字を含めてください。")
		@Size(max = 72, message = "パスワードは72文字以内で入力してください。")
		String password,

		@NotNull(message = "パスワード（確認）を入力してください。")
		String passwordConfirm) {
}
