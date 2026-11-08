package com.mindlog.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 1日の振り返りモードの入力内容（daily_review_details テーブル）。
 */
@Entity
@Table(name = "daily_review_details")
public class DailyReviewDetails {

	@Id
	@Column(name = "record_id")
	private Long recordId;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(columnDefinition = "TEXT")
	private String events;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(columnDefinition = "TEXT")
	private String reflection;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(columnDefinition = "TEXT")
	private String learning;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(columnDefinition = "TEXT")
	private String insight;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "tomorrow_tasks", columnDefinition = "TEXT")
	private String tomorrowTasks;

	/** JPA が使うための引数なしコンストラクタ */
	protected DailyReviewDetails() {
	}

	public DailyReviewDetails(Long recordId, Map<String, String> values) {
		this.recordId = recordId;
		this.events = values.get("events");
		this.reflection = values.get("reflection");
		this.learning = values.get("learning");
		this.insight = values.get("insight");
		this.tomorrowTasks = values.get("tomorrowTasks");
	}

	/** API の details の形（未入力の項目は null）に変換する */
	public Map<String, String> toMap() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("events", events);
		map.put("reflection", reflection);
		map.put("learning", learning);
		map.put("insight", insight);
		map.put("tomorrowTasks", tomorrowTasks);
		return map;
	}
}
