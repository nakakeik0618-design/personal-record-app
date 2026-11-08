package com.mindlog.entity;

import java.util.List;

/**
 * 記録のモード（API設計書 2.4節）と、モードごとの入力項目（API設計書 5.1節）。
 */
public enum RecordMode {

	PROBLEM_SOLVING(List.of("goal", "currentState", "gap", "cause", "countermeasure")),
	DIARY(List.of("memo")),
	DAILY_REVIEW(List.of("events", "reflection", "learning", "insight", "tomorrowTasks"));

	private final List<String> detailKeys;

	RecordMode(List<String> detailKeys) {
		this.detailKeys = detailKeys;
	}

	public List<String> getDetailKeys() {
		return detailKeys;
	}
}
