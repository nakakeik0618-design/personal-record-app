package com.mindlog.controller;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * 記録API（API-07, API-08）のテスト。
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
