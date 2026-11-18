package com.mindlog.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mindlog.dto.ErrorResponse.FieldErrorResponse;
import com.mindlog.dto.RecordRequest;
import com.mindlog.dto.RecordResponse;
import com.mindlog.dto.RecordSummaryResponse;
import com.mindlog.entity.DailyReviewDetails;
import com.mindlog.entity.DiaryDetails;
import com.mindlog.entity.ProblemSolvingDetails;
import com.mindlog.entity.RecordEntity;
import com.mindlog.entity.RecordMode;
import com.mindlog.exception.InputValidationException;
import com.mindlog.exception.RecordNotFoundException;
import com.mindlog.exception.SearchConditionException;
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
		List<FieldErrorResponse> errors = new ArrayList<>();
		if (request.mode() == null) {
			errors.add(new FieldErrorResponse("mode", "モードを選択してください。"));
		}
		validate(request.mode(), request, requestDetails, errors);

		RecordEntity record = recordRepository.saveAndFlush(new RecordEntity(userId, request.mode(),
				blankToNull(request.title()), request.targetDate()));
		return RecordResponse.from(record, saveDetails(record, toSavedValues(requestDetails)));
	}

	/**
	 * 記録を更新する（FR-013, FR-014）。リクエストの mode は無視し、記録のモードのまま更新する。
	 * details に含まれない項目は未入力（null）になる。
	 */
	@Transactional
	public RecordResponse update(Long userId, Long id, RecordRequest request) {
		RecordEntity record = recordRepository.findByIdAndUserId(id, userId)
				.orElseThrow(RecordNotFoundException::new);
		Map<String, String> requestDetails = request.details() == null ? Map.of() : request.details();
		validate(record.getMode(), request, requestDetails, new ArrayList<>());

		record.update(blankToNull(request.title()), request.targetDate());
		// saveAndFlush：更新を DB にすぐ反映する（トランザクションの終わりを待たない）
		recordRepository.saveAndFlush(record);
		return RecordResponse.from(record, saveDetails(record, toSavedValues(requestDetails)));
	}

	/**
	 * 記録を削除する（FR-015）。モード別テーブルの行は、外部キーの ON DELETE CASCADE で一緒に削除される。
	 */
	@Transactional
	public void delete(Long userId, Long id) {
		RecordEntity record = recordRepository.findByIdAndUserId(id, userId)
				.orElseThrow(RecordNotFoundException::new);
		recordRepository.delete(record);
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

	/**
	 * 最近の記録を取得する（FR-017）。
	 */
	@Transactional(readOnly = true)
	public List<RecordSummaryResponse> recent(Long userId) {
		// 省略しない形：
		// List<RecordSummaryResponse> result = new ArrayList<>();
		// for (RecordEntity record : recordRepository.findTop5ByUserIdOrderByUpdatedAtDescIdDesc(userId)) {
		//     result.add(RecordSummaryResponse.from(record));
		// }
		// return result;
		return recordRepository.findTop5ByUserIdOrderByUpdatedAtDescIdDesc(userId).stream()
				.map(RecordSummaryResponse::from)
				.toList();
	}

	/**
	 * 記録を検索する（FR-018, FR-020〜FR-024）。null の条件は検索条件に含めない。
	 */
	@Transactional(readOnly = true)
	public List<RecordSummaryResponse> search(Long userId, RecordMode mode, LocalDate from, LocalDate to,
			String title) {
		if ((from == null) != (to == null)) {
			throw new SearchConditionException("開始日と終了日は両方指定してください。");
		}
		if (from != null && from.isAfter(to)) {
			throw new SearchConditionException("開始日は終了日以前の日付を指定してください。");
		}
		String titlePattern = title == null || title.isEmpty() ? null : "%" + escapeLike(title) + "%";
		return recordRepository.search(userId, mode, from, to, titlePattern).stream()
				.map(RecordSummaryResponse::from)
				.toList();
	}

	/**
	 * 入力内容をチェックし、エラーがあれば InputValidationException を投げる。
	 * mode は入力項目の確認に使うモード（作成時はリクエストの値、更新時は記録のモード）。
	 * errors には、呼び出し元で見つけたエラーを入れて渡す。
	 */
	private void validate(RecordMode mode, RecordRequest request, Map<String, String> details,
			List<FieldErrorResponse> errors) {
		if (request.targetDate() == null) {
			errors.add(new FieldErrorResponse("targetDate", "対象日を入力してください。"));
		}
		if (length(request.title()) > TITLE_MAX_LENGTH) {
			errors.add(new FieldErrorResponse("title", "タイトルは30文字以内で入力してください。"));
		}
		if (mode != null) {
			details.forEach((key, value) -> {
				if (!mode.getDetailKeys().contains(key)) {
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

	/** 保存する値に変換する（未入力は null） */
	private static Map<String, String> toSavedValues(Map<String, String> requestDetails) {
		Map<String, String> values = new HashMap<>();
		requestDetails.forEach((key, value) -> values.put(key, blankToNull(value)));
		return values;
	}

	/** モード別テーブルに保存する。すでに行がある場合（更新時）は上書きする */
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

	/** LIKE で特別な意味を持つ文字（\ % _）を、ただの文字として検索できるようにする */
	private static String escapeLike(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	/** 未入力は null として保存する（API設計書 5.1節） */
	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}
}
