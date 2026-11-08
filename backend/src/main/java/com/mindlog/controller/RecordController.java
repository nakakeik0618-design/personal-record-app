package com.mindlog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mindlog.dto.RecordRequest;
import com.mindlog.dto.RecordResponse;
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
