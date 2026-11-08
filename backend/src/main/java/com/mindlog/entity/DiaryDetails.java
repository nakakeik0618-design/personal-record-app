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
 * 日記モードの入力内容（diary_details テーブル）。
 */
@Entity
@Table(name = "diary_details")
public class DiaryDetails {

	@Id
	@Column(name = "record_id")
	private Long recordId;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(columnDefinition = "TEXT")
	private String memo;

	/** JPA が使うための引数なしコンストラクタ */
	protected DiaryDetails() {
	}

	public DiaryDetails(Long recordId, Map<String, String> values) {
		this.recordId = recordId;
		this.memo = values.get("memo");
	}

	/** API の details の形（未入力の項目は null）に変換する */
	public Map<String, String> toMap() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("memo", memo);
		return map;
	}
}
