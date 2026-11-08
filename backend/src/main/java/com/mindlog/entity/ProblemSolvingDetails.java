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
 * 問題解決モードの入力内容（problem_solving_details テーブル）。
 */
@Entity
@Table(name = "problem_solving_details")
public class ProblemSolvingDetails {

	@Id
	@Column(name = "record_id")
	private Long recordId;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(columnDefinition = "TEXT")
	private String goal;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "current_state", columnDefinition = "TEXT")
	private String currentState;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(columnDefinition = "TEXT")
	private String gap;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(columnDefinition = "TEXT")
	private String cause;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(columnDefinition = "TEXT")
	private String countermeasure;

	/** JPA が使うための引数なしコンストラクタ */
	protected ProblemSolvingDetails() {
	}

	public ProblemSolvingDetails(Long recordId, Map<String, String> values) {
		this.recordId = recordId;
		this.goal = values.get("goal");
		this.currentState = values.get("currentState");
		this.gap = values.get("gap");
		this.cause = values.get("cause");
		this.countermeasure = values.get("countermeasure");
	}

	/** API の details の形（未入力の項目は null）に変換する */
	public Map<String, String> toMap() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("goal", goal);
		map.put("currentState", currentState);
		map.put("gap", gap);
		map.put("cause", cause);
		map.put("countermeasure", countermeasure);
		return map;
	}
}
