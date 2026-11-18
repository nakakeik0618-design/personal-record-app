package com.mindlog.controller;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.mindlog.entity.User;
import com.mindlog.repository.UserRepository;
import com.mindlog.security.LoginUser;

/**
 * 記録API（API-05〜API-10）のテスト。
 * 各テストはトランザクション内で実行し、終了時にロールバックするため DB にデータは残らない。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecordControllerTest {

	private static final String DATE_TIME = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	UserRepository userRepository;

	private LoginUser owner;
	private LoginUser other;

	@BeforeEach
	void setUp() {
		owner = saveUser("record-owner@example.com");
		other = saveUser("record-other@example.com");
	}

	// ---- API-08 記録作成 ----

	@Test
	void 問題解決の記録を作成でき_未入力の項目はnullになる() throws Exception {
		create(owner, """
				{"mode": "PROBLEM_SOLVING", "title": "段取りの見直し", "targetDate": "2026-10-10",
				 "details": {"goal": "目標", "gap": ""}}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.mode").value("PROBLEM_SOLVING"))
				.andExpect(jsonPath("$.title").value("段取りの見直し"))
				.andExpect(jsonPath("$.targetDate").value("2026-10-10"))
				.andExpect(jsonPath("$.createdAt").value(matchesPattern(DATE_TIME)))
				.andExpect(jsonPath("$.updatedAt").value(matchesPattern(DATE_TIME)))
				.andExpect(jsonPath("$.details.goal").value("目標"))
				.andExpect(jsonPath("$.details.gap").value(nullValue()))
				.andExpect(jsonPath("$.details.countermeasure").value(nullValue()));
	}

	@Test
	void 日記はタイトルと本文が未入力でも作成できる() throws Exception {
		create(owner, """
				{"mode": "DIARY", "title": "", "targetDate": "2026-10-10"}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value(nullValue()))
				.andExpect(jsonPath("$.details.memo").value(nullValue()));
	}

	@Test
	void 振り返りの記録を作成して詳細を取得できる() throws Exception {
		String body = create(owner, """
				{"mode": "DAILY_REVIEW", "targetDate": "2026-10-10",
				 "details": {"events": "出来事", "tomorrowTasks": "明日のこと"}}
				""")
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		Integer id = JsonPath.read(body, "$.id");

		mockMvc.perform(get("/api/records/" + id).with(user(owner)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mode").value("DAILY_REVIEW"))
				.andExpect(jsonPath("$.details.events").value("出来事"))
				.andExpect(jsonPath("$.details.tomorrowTasks").value("明日のこと"))
				.andExpect(jsonPath("$.details.reflection").value(nullValue()));
	}

	@Test
	void モードと対象日は必須() throws Exception {
		create(owner, "{}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[?(@.field == 'mode')]").exists())
				.andExpect(jsonPath("$.errors[?(@.field == 'targetDate')]").exists());
	}

	@Test
	void タイトルは30文字まで_絵文字も1文字と数える() throws Exception {
		create(owner, diaryJson("😀".repeat(30), "本文"))
				.andExpect(status().isCreated());

		create(owner, diaryJson("あ".repeat(31), "本文"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("title"))
				.andExpect(jsonPath("$.errors[0].message").value("タイトルは30文字以内で入力してください。"));
	}

	@Test
	void 入力項目は5000文字まで() throws Exception {
		create(owner, diaryJson("題", "あ".repeat(5000)))
				.andExpect(status().isCreated());

		create(owner, diaryJson("題", "あ".repeat(5001)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("details.memo"));
	}

	@Test
	void モードに対応しない項目は400を返す() throws Exception {
		create(owner, """
				{"mode": "DIARY", "targetDate": "2026-10-10", "details": {"goal": "x"}}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("details.goal"));
	}

	@Test
	void 存在しないモードは400を返す() throws Exception {
		create(owner, """
				{"mode": "UNKNOWN", "targetDate": "2026-10-10"}
				""")
				.andExpect(status().isBadRequest());
	}

	@Test
	void 未ログインでは作成も取得もできない() throws Exception {
		mockMvc.perform(post("/api/records").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(diaryJson("題", "本文")))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/records/1"))
				.andExpect(status().isUnauthorized());
	}

	// ---- API-07 記録詳細取得 ----

	@Test
	void 他ユーザーの記録は404を返す() throws Exception {
		String body = create(owner, diaryJson("題", "本文"))
				.andReturn().getResponse().getContentAsString();
		Integer id = JsonPath.read(body, "$.id");

		mockMvc.perform(get("/api/records/" + id).with(user(other)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("記録が見つかりません。"));
	}

	@Test
	void 存在しない記録と数値でないIDは404を返す() throws Exception {
		mockMvc.perform(get("/api/records/999999999").with(user(owner)))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/records/abc").with(user(owner)))
				.andExpect(status().isNotFound());
	}

	// ---- API-05 最近の記録取得 ----

	@Test
	void 最近の記録は新しい順に5件まで_detailsは含まない() throws Exception {
		for (int i = 1; i <= 6; i++) {
			create(owner, diaryJson("記録" + i, "本文"));
		}
		create(other, diaryJson("他人の記録", "本文"));

		mockMvc.perform(get("/api/records/recent").with(user(owner)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(5))
				.andExpect(jsonPath("$[0].title").value("記録6"))
				.andExpect(jsonPath("$[4].title").value("記録2"))
				.andExpect(jsonPath("$[0].createdAt").value(matchesPattern(DATE_TIME)))
				.andExpect(jsonPath("$[0].details").doesNotExist());
	}

	@Test
	void 記録が0件なら空配列を返す() throws Exception {
		mockMvc.perform(get("/api/records/recent").with(user(owner)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
		mockMvc.perform(get("/api/records").with(user(owner)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	// ---- API-06 記録一覧・検索 ----

	@Test
	void 条件なしなら自分の記録をすべて返す() throws Exception {
		createRecord(owner, "DIARY", "日記1", "2026-10-01");
		createRecord(owner, "PROBLEM_SOLVING", "問題1", "2026-10-02");
		createRecord(other, "DIARY", "他人の記録", "2026-10-01");

		mockMvc.perform(get("/api/records").with(user(owner)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].title").value("問題1"))
				.andExpect(jsonPath("$[1].title").value("日記1"));
	}

	@Test
	void モード_対象日_タイトルをANDで検索できる() throws Exception {
		createRecord(owner, "DIARY", "仕事の日記", "2026-10-10");
		createRecord(owner, "DIARY", "仕事の日記（範囲外）", "2026-11-01");
		createRecord(owner, "DIARY", "休日の日記", "2026-10-10");
		createRecord(owner, "PROBLEM_SOLVING", "仕事の問題", "2026-10-10");

		mockMvc.perform(get("/api/records").with(user(owner))
				.param("mode", "DIARY")
				.param("from", "2026-10-01")
				.param("to", "2026-10-31")
				.param("title", "仕事"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].title").value("仕事の日記"));
	}

	@Test
	void 開始日と終了日が同じ日でも検索できる() throws Exception {
		createRecord(owner, "DIARY", "当日", "2026-10-10");
		createRecord(owner, "DIARY", "前日", "2026-10-09");

		mockMvc.perform(get("/api/records").with(user(owner))
				.param("from", "2026-10-10")
				.param("to", "2026-10-10"))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].title").value("当日"));
	}

	@Test
	void タイトルの記号はそのままの文字として検索する() throws Exception {
		createRecord(owner, "DIARY", "達成率100%", "2026-10-10");
		createRecord(owner, "DIARY", "達成率1000", "2026-10-10");

		mockMvc.perform(get("/api/records").with(user(owner)).param("title", "0%"))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].title").value("達成率100%"));
	}

	@Test
	void 空のタイトルは条件に含めない() throws Exception {
		createRecord(owner, "DIARY", "", "2026-10-10");

		mockMvc.perform(get("/api/records").with(user(owner)).param("title", ""))
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void 開始日と終了日の片方だけでは400を返す() throws Exception {
		mockMvc.perform(get("/api/records").with(user(owner)).param("from", "2026-10-01"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/records").with(user(owner)).param("to", "2026-10-01"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void 開始日が終了日より後なら400を返す() throws Exception {
		mockMvc.perform(get("/api/records").with(user(owner))
				.param("from", "2026-10-31")
				.param("to", "2026-10-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("開始日は終了日以前の日付を指定してください。"))
				.andExpect(jsonPath("$.errors.length()").value(0));
	}

	@Test
	void 存在しないモードや日付でない値は400を返す() throws Exception {
		mockMvc.perform(get("/api/records").with(user(owner)).param("mode", "UNKNOWN"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/records").with(user(owner))
				.param("from", "abc")
				.param("to", "2026-10-01"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void 未ログインでは一覧も最近の記録も取得できない() throws Exception {
		mockMvc.perform(get("/api/records"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/records/recent"))
				.andExpect(status().isUnauthorized());
	}

	// ---- API-09 記録更新 ----

	@Test
	void 記録を更新でき_モードは変わらない() throws Exception {
		Integer id = createId(owner, """
				{"mode": "PROBLEM_SOLVING", "title": "元のタイトル", "targetDate": "2026-10-10",
				 "details": {"goal": "元の目標", "cause": "元の原因"}}
				""");

		update(owner, id, """
				{"mode": "DIARY", "title": "新しいタイトル", "targetDate": "2026-10-11",
				 "details": {"goal": "新しい目標", "gap": "ギャップ"}}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mode").value("PROBLEM_SOLVING"))
				.andExpect(jsonPath("$.title").value("新しいタイトル"))
				.andExpect(jsonPath("$.targetDate").value("2026-10-11"))
				.andExpect(jsonPath("$.details.goal").value("新しい目標"))
				.andExpect(jsonPath("$.details.gap").value("ギャップ"))
				// 送らなかった項目は未入力になる
				.andExpect(jsonPath("$.details.cause").value(nullValue()));

		mockMvc.perform(get("/api/records/" + id).with(user(owner)))
				.andExpect(jsonPath("$.mode").value("PROBLEM_SOLVING"))
				.andExpect(jsonPath("$.title").value("新しいタイトル"))
				.andExpect(jsonPath("$.details.goal").value("新しい目標"))
				.andExpect(jsonPath("$.details.cause").value(nullValue()));
	}

	@Test
	void タイトルを空にすると未入力になる() throws Exception {
		Integer id = createId(owner, diaryJson("題", "本文"));

		update(owner, id, """
				{"title": "", "targetDate": "2026-10-10", "details": {"memo": "本文"}}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value(nullValue()));
	}

	@Test
	void 更新でも入力チェックを行う() throws Exception {
		Integer id = createId(owner, diaryJson("題", "本文"));

		update(owner, id, """
				{"title": "%s", "details": {"goal": "x"}}
				""".formatted("あ".repeat(31)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[?(@.field == 'targetDate')]").exists())
				.andExpect(jsonPath("$.errors[?(@.field == 'title')]").exists())
				.andExpect(jsonPath("$.errors[?(@.field == 'details.goal')]").exists());
	}

	@Test
	void 他ユーザーや存在しない記録は更新できない() throws Exception {
		Integer id = createId(owner, diaryJson("題", "本文"));

		update(other, id, diaryJson("乗っ取り", "本文"))
				.andExpect(status().isNotFound());
		update(owner, 999999999, diaryJson("題", "本文"))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/records/" + id).with(user(owner)))
				.andExpect(jsonPath("$.title").value("題"));
	}

	// ---- API-10 記録削除 ----

	@Test
	void 記録を削除すると取得できなくなる() throws Exception {
		Integer id = createId(owner, diaryJson("題", "本文"));

		mockMvc.perform(delete("/api/records/" + id).with(user(owner)).with(csrf()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/records/" + id).with(user(owner)))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/records").with(user(owner)))
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void 他ユーザーや存在しない記録は削除できない() throws Exception {
		Integer id = createId(owner, diaryJson("題", "本文"));

		mockMvc.perform(delete("/api/records/" + id).with(user(other)).with(csrf()))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/records/999999999").with(user(owner)).with(csrf()))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/records/" + id).with(user(owner)))
				.andExpect(status().isOk());
	}

	@Test
	void CSRFトークンがないと更新も削除もできない() throws Exception {
		Integer id = createId(owner, diaryJson("題", "本文"));

		mockMvc.perform(put("/api/records/" + id).with(user(owner))
				.contentType(MediaType.APPLICATION_JSON)
				.content(diaryJson("題", "本文")))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete("/api/records/" + id).with(user(owner)))
				.andExpect(status().isForbidden());
	}

	private Integer createId(LoginUser loginUser, String json) throws Exception {
		String body = create(loginUser, json)
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.id");
	}

	private ResultActions update(LoginUser loginUser, Integer id, String json) throws Exception {
		return mockMvc.perform(put("/api/records/" + id).with(user(loginUser)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	private void createRecord(LoginUser loginUser, String mode, String title, String targetDate) throws Exception {
		create(loginUser, """
				{"mode": "%s", "title": "%s", "targetDate": "%s"}
				""".formatted(mode, title, targetDate))
				.andExpect(status().isCreated());
	}

	private LoginUser saveUser(String email) {
		return new LoginUser(userRepository.saveAndFlush(new User(email, "hash")));
	}

	private ResultActions create(LoginUser loginUser, String json) throws Exception {
		return mockMvc.perform(post("/api/records").with(user(loginUser)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	private static String diaryJson(String title, String memo) {
		return """
				{"mode": "DIARY", "title": "%s", "targetDate": "2026-10-10", "details": {"memo": "%s"}}
				""".formatted(title, memo);
	}
}
