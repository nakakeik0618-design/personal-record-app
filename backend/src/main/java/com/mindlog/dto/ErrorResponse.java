package com.mindlog.dto;

import java.util.List;

/**
 * エラー時のレスポンス（API設計書 2.2節）。
 */
public record ErrorResponse(String message, List<FieldErrorResponse> errors) {

	public record FieldErrorResponse(String field, String message) {
	}

	/** 項目に紐付かないエラー */
	public static ErrorResponse of(String message) {
		return new ErrorResponse(message, List.of());
	}
}
