package com.mindlog.dto;

import java.time.LocalDate;
import java.util.Map;

import com.mindlog.entity.RecordMode;

/**
 * 記録作成のリクエスト（API-08）。
 * details の項目がモードによって変わるため、入力チェックは RecordService でまとめて行う。
 */
public record RecordRequest(RecordMode mode, String title, LocalDate targetDate, Map<String, String> details) {
}
