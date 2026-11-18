package com.mindlog.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mindlog.entity.RecordEntity;
import com.mindlog.entity.RecordMode;

/**
 * 一覧用の記録データ（API設計書 5.1節。API-05, API-06）。details は含めない。
 */
public record RecordSummaryResponse(Long id, RecordMode mode, String title, LocalDate targetDate,
		LocalDateTime createdAt, LocalDateTime updatedAt) {

	public static RecordSummaryResponse from(RecordEntity record) {
		return new RecordSummaryResponse(record.getId(), record.getMode(), record.getTitle(), record.getTargetDate(),
				record.getCreatedAt(), record.getUpdatedAt());
	}
}
