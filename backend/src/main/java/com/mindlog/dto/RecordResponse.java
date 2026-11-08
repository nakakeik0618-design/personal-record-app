package com.mindlog.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import com.mindlog.entity.RecordEntity;
import com.mindlog.entity.RecordMode;

/**
 * 記録データ（API設計書 5.1節）。
 */
public record RecordResponse(Long id, RecordMode mode, String title, LocalDate targetDate, LocalDateTime createdAt,
		LocalDateTime updatedAt, Map<String, String> details) {

	public static RecordResponse from(RecordEntity record, Map<String, String> details) {
		return new RecordResponse(record.getId(), record.getMode(), record.getTitle(), record.getTargetDate(),
				record.getCreatedAt(), record.getUpdatedAt(), details);
	}
}
