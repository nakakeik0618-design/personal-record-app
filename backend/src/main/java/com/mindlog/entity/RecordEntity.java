package com.mindlog.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * 記録の全モード共通の項目（records テーブル）。
 * Java 標準の java.lang.Record と紛らわしいため、クラス名は RecordEntity とする。
 */
@Entity
@Table(name = "records")
public class RecordEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	/** テーブルの型は VARCHAR のため、Hibernate 既定の ENUM 型にならないよう指定する */
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, updatable = false, length = 20)
	private RecordMode mode;

	@Column(length = 30)
	private String title;

	@Column(name = "target_date", nullable = false)
	private LocalDate targetDate;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	/** JPA が使うための引数なしコンストラクタ */
	protected RecordEntity() {
	}

	public RecordEntity(Long userId, RecordMode mode, String title, LocalDate targetDate) {
		this.userId = userId;
		this.mode = mode;
		this.title = title;
		this.targetDate = targetDate;
	}

	/** DATETIME は秒までのため、API の応答とDBの値がずれないよう秒で切り捨てる */
	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		this.createdAt = now;
		this.updatedAt = now;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public RecordMode getMode() {
		return mode;
	}

	public String getTitle() {
		return title;
	}

	public LocalDate getTargetDate() {
		return targetDate;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
