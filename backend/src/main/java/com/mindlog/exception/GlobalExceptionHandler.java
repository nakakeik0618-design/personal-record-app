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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.mindlog.dto.ErrorResponse;
import com.mindlog.dto.ErrorResponse.FieldErrorResponse;

/**
 * 例外を API設計書 2.2節のエラーレスポンス形式に変換する。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String INVALID_INPUT = "入力内容に誤りがあります。";
	private static final String RECORD_NOT_FOUND = "記録が見つかりません。";

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

	/** 複数の項目にまたがる入力エラー（400） */
	@ExceptionHandler(InputValidationException.class)
	public ResponseEntity<ErrorResponse> handleInputValidation(InputValidationException e) {
		return ResponseEntity.badRequest().body(new ErrorResponse(INVALID_INPUT, e.getErrors()));
	}

	/** 記録が存在しない、または他ユーザーの記録（404） */
	@ExceptionHandler(RecordNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleRecordNotFound(RecordNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(RECORD_NOT_FOUND));
	}

	/** URL の記録IDが数値でない場合も、存在しない記録として扱う（404） */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(RECORD_NOT_FOUND));
	}
}
