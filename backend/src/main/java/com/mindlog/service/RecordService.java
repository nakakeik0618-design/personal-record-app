package com.mindlog.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mindlog.dto.ErrorResponse.FieldErrorResponse;
import com.mindlog.dto.RecordRequest;
import com.mindlog.dto.RecordResponse;
import com.mindlog.entity.DailyReviewDetails;
import com.mindlog.entity.DiaryDetails;
import com.mindlog.entity.ProblemSolvingDetails;
import com.mindlog.entity.RecordEntity;
import com.mindlog.exception.InputValidationException;
import com.mindlog.exception.RecordNotFoundException;
import com.mindlog.repository.DailyReviewDetailsRepository;
import com.mindlog.repository.DiaryDetailsRepository;
import com.mindlog.repository.ProblemSolvingDetailsRepository;
import com.mindlog.repository.RecordRepository;

@Service
public class RecordService {

	private static final int TITLE_MAX_LENGTH = 30;
	private static final int DETAIL_MAX_LENGTH = 5000;

	private final RecordRepository recordRepository;
	private final ProblemSolvingDetailsRepository problemSolvingDetailsRepository;
	private final DiaryDetailsRepository diaryDetailsRepository;
	private final DailyReviewDetailsRepository dailyReviewDetailsRepository;

	public RecordService(RecordRepository recordRepository,
			ProblemSolvingDetailsRepository problemSolvingDetailsRepository,
			DiaryDetailsRepository diaryDetailsRepository,
			DailyReviewDetailsRepository dailyReviewDetailsRepository) {
		this.recordRepository = recordRepository;
		this.problemSolvingDetailsRepository = problemSolvingDetailsRepository;
		this.diaryDetailsRepository = diaryDetailsRepository;
		this.dailyReviewDetailsRepository = dailyReviewDetailsRepository;
	}

	/**
	 * 記録を作成する（FR-006〜FR-012）。
	 */
	@Transactional
	public RecordResponse create(Long userId, RecordRequest request) {
		Map<String, String> requestDetails = request.details() == null ? Map.of() : request.details();
		validate(request, requestDetails);

		RecordEntity record = recordRepository.saveAndFlush(new RecordEntity(userId, request.mode(),
				blankToNull(request.title()), request.targetDate()));

		Map<String, String> details = new HashMap<>();
		requestDetails.forEach((key, value) -> details.put(key, blankToNull(value)));
		return RecordResponse.from(record, saveDetails(record, details));
	}

	/**
	 * 記録の詳細を取得する（FR-019）。
	 */
	@Transactional(readOnly = true)
	public RecordResponse get(Long userId, Long id) {
		RecordEntity record = recordRepository.findByIdAndUserId(id, userId)
				.orElseThrow(RecordNotFoundException::new);
		return RecordResponse.from(record, findDetails(record));
	}

	private void validate(RecordRequest request, Map<String, String> details) {
		List<FieldErrorResponse> errors = new ArrayList<>();
		if (request.mode() == null) {
			errors.add(new FieldErrorResponse("mode", "モードを選択してください。"));
		}
		if (request.targetDate() == null) {
			errors.add(new FieldErrorResponse("targetDate", "対象日を入力してください。"));
		}
		if (length(request.title()) > TITLE_MAX_LENGTH) {
			errors.add(new FieldErrorResponse("title", "タイトルは30文字以内で入力してください。"));
		}
		if (request.mode() != null) {
			details.forEach((key, value) -> {
				if (!request.mode().getDetailKeys().contains(key)) {
					errors.add(new FieldErrorResponse("details." + key, "このモードでは入力できない項目です。"));
				} else if (length(value) > DETAIL_MAX_LENGTH) {
					errors.add(new FieldErrorResponse("details." + key, "5,000文字以内で入力してください。"));
				}
			});
		}
		if (!errors.isEmpty()) {
			throw new InputValidationException(errors);
		}
	}

	private Map<String, String> saveDetails(RecordEntity record, Map<String, String> values) {
		Long id = record.getId();
		return switch (record.getMode()) {
			case PROBLEM_SOLVING -> problemSolvingDetailsRepository.save(new ProblemSolvingDetails(id, values)).toMap();
			case DIARY -> diaryDetailsRepository.save(new DiaryDetails(id, values)).toMap();
			case DAILY_REVIEW -> dailyReviewDetailsRepository.save(new DailyReviewDetails(id, values)).toMap();
		};
	}

	private Map<String, String> findDetails(RecordEntity record) {
		Long id = record.getId();
		return switch (record.getMode()) {
			case PROBLEM_SOLVING -> problemSolvingDetailsRepository.findById(id).orElseThrow().toMap();
			case DIARY -> diaryDetailsRepository.findById(id).orElseThrow().toMap();
			case DAILY_REVIEW -> dailyReviewDetailsRepository.findById(id).orElseThrow().toMap();
		};
	}

	/** 絵文字なども1文字と数える（DBの VARCHAR の文字数と合わせる） */
	private static int length(String value) {
		return value == null ? 0 : value.codePointCount(0, value.length());
	}

	/** 未入力は null として保存する（API設計書 5.1節） */
	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}
}
