package com.mindlog.exception;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mindlog.dto.ErrorResponse;
import com.mindlog.dto.ErrorResponse.FieldErrorResponse;

/**
 * 例外を API設計書 2.2節のエラーレスポンス形式に変換する。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String INVALID_INPUT = "入力内容に誤りがあります。";

	/** @Valid による入力チェックのエラー（400） */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		// 1つの項目に複数のエラーがある場合は、最初の1つだけを返す
		Map<String, String> messages = new LinkedHashMap<>();
		e.getBindingResult().getFieldErrors()
				.forEach(error -> messages.putIfAbsent(error.getField(), error.getDefaultMessage()));
		List<FieldErrorResponse> errors = messages.entrySet().stream()
				.map(entry -> new FieldErrorResponse(entry.getKey(), entry.getValue()))
				.toList();
		return ResponseEntity.badRequest().body(new ErrorResponse(INVALID_INPUT, errors));
	}

	/** 項目に紐付くエラー（パスワード不一致 400、メールアドレス重複 409 など） */
	@ExceptionHandler(FieldValidationException.class)
	public ResponseEntity<ErrorResponse> handleFieldValidation(FieldValidationException e) {
		ErrorResponse body = new ErrorResponse(INVALID_INPUT,
				List.of(new FieldErrorResponse(e.getField(), e.getMessage())));
		return ResponseEntity.status(e.getStatus()).body(body);
	}

	/** リクエストの JSON が読み取れない（400） */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(INVALID_INPUT));
	}
}
