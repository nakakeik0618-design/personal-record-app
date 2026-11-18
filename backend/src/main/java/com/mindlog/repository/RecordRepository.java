package com.mindlog.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindlog.entity.RecordEntity;
import com.mindlog.entity.RecordMode;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

	/** ログイン中のユーザー自身の記録のみを取得する（FR-027） */
	Optional<RecordEntity> findByIdAndUserId(Long id, Long userId);

	/**
	 * 最近の記録（FR-017）。更新日時の新しい順に5件。
	 * メソッド名から SQL が作られる（Top5 = 先頭5件、OrderBy… = 並び順。同じ更新日時なら新しく作った順）
	 */
	List<RecordEntity> findTop5ByUserIdOrderByUpdatedAtDescIdDesc(Long userId);

	/**
	 * 記録一覧・検索（FR-018, FR-020〜FR-024）。
	 * 引数が null の条件は「:mode is null or …」の部分が常に真になるため、条件に含まれない。
	 * titlePattern は「%キーワード%」の形で渡す（% と _ は \ でエスケープ済み）。
	 */
	@Query("""
			select r from RecordEntity r
			where r.userId = :userId
			  and (:mode is null or r.mode = :mode)
			  and (:from is null or r.targetDate >= :from)
			  and (:to is null or r.targetDate <= :to)
			  and (:titlePattern is null or r.title like :titlePattern escape '\\')
			order by r.updatedAt desc, r.id desc
			""")
	List<RecordEntity> search(@Param("userId") Long userId, @Param("mode") RecordMode mode,
			@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("titlePattern") String titlePattern);
}
