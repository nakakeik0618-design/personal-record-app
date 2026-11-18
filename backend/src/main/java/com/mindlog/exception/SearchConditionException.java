package com.mindlog.exception;

/**
 * 記録一覧の検索条件が正しくない（API-06。400、項目に紐付かないエラー）。
 */
public class SearchConditionException extends RuntimeException {

	public SearchConditionException(String message) {
		super(message);
	}
}
