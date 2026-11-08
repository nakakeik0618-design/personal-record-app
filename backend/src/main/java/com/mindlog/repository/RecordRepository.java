package com.mindlog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mindlog.entity.RecordEntity;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

	/** ログイン中のユーザー自身の記録のみを取得する（FR-027） */
	Optional<RecordEntity> findByIdAndUserId(Long id, Long userId);
}
