package com.mindlog.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mindlog.dto.RecordRequest;
import com.mindlog.dto.RecordResponse;
import com.mindlog.dto.RecordSummaryResponse;
import com.mindlog.entity.RecordMode;
import com.mindlog.security.LoginUser;
import com.mindlog.service.RecordService;

/**
 * 記録API（API設計書 5節）。
 */
@RestController
@RequestMapping("/api/records")
public class RecordController {

	private final RecordService recordService;

	public RecordController(RecordService recordService) {
		this.recordService = recordService;
	}

	/** API-05 最近の記録取得 */
	@GetMapping("/recent")
	public List<RecordSummaryResponse> recent(@AuthenticationPrincipal LoginUser loginUser) {
		return recordService.recent(loginUser.getId());
	}

	/**
	 * API-06 記録一覧・検索。
	 * 例：/api/records?mode=DIARY&from=2026-10-01&to=2026-10-31&title=仕事
	 * required = false：URL に無いパラメータは null になる
	 */
	@GetMapping
	public List<RecordSummaryResponse> search(@AuthenticationPrincipal LoginUser loginUser,
			@RequestParam(required = false) RecordMode mode,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
			@RequestParam(required = false) String title) {
		return recordService.search(loginUser.getId(), mode, from, to, title);
	}

	/** API-07 記録詳細取得 */
	@GetMapping("/{id}")
	public RecordResponse get(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
		return recordService.get(loginUser.getId(), id);
	}

	/** API-08 記録作成 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RecordResponse create(@AuthenticationPrincipal LoginUser loginUser, @RequestBody RecordRequest request) {
		return recordService.create(loginUser.getId(), request);
	}
}
