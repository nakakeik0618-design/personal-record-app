package com.mindlog.exception;

import org.springframework.http.HttpStatus;

/**
 * 特定の項目に関するエラー（パスワード不一致、メールアドレス重複など）。
 */
public class FieldValidationException extends RuntimeException {

	private final HttpStatus status;
	private final String field;

	public FieldValidationException(HttpStatus status, String field, String message) {
		super(message);
		this.status = status;
		this.field = field;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getField() {
		return field;
	}
}
