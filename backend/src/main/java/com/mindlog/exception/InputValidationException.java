package com.mindlog.exception;

import java.util.List;

import com.mindlog.dto.ErrorResponse.FieldErrorResponse;

/**
 * 複数の項目にまたがる入力エラー（400）。
 */
public class InputValidationException extends RuntimeException {

	private final List<FieldErrorResponse> errors;

	public InputValidationException(List<FieldErrorResponse> errors) {
		this.errors = errors;
	}

	public List<FieldErrorResponse> getErrors() {
		return errors;
	}
}
