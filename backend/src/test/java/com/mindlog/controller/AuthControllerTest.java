package com.mindlog.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.mindlog.entity.User;
import com.mindlog.repository.UserRepository;

/**
 * 認証API（API-01〜04）のテスト。
 * 各テストはトランザクション内で実行し、終了時にロールバックするため DB にデータは残らない。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

	private static final String EMAIL = "test-user@example.com";
	private static final String PASSWORD = "password123";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	UserRepository userRepository;

	// ---- API-01 ユーザー登録 ----

	@Test
	void 登録に成功すると201を返し_メールアドレスは小文字_パスワードはハッシュ化して保存する() throws Exception {
		register("Test-User@Example.com", PASSWORD, PASSWORD)
				.andExpect(status().isCreated());

		User saved = userRepository.findByEmail(EMAIL).orElseThrow();
		assertThat(saved.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2");
	}

	@Test
	void メールアドレスの形式とパスワードの入力ルールに違反すると400を返す() throws Exception {
		register("invalid-email", "short", "short")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[?(@.field == 'email')].message").value("メールアドレスの形式が正しくありません。"))
				.andExpect(jsonPath("$.errors[?(@.field == 'password')].message")
						.value("パスワードは8文字以上で、英字と数字を含めてください。"));
	}

	@Test
	void 数字を含まないパスワードは400を返す() throws Exception {
		register(EMAIL, "passwordonly", "passwordonly")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("password"));
	}

	@Test
	void パスワードとパスワード確認が一致しないと400を返す() throws Exception {
		register(EMAIL, PASSWORD, "password124")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("passwordConfirm"))
				.andExpect(jsonPath("$.errors[0].message").value("パスワードが一致しません。"));
	}

	@Test
	void 登録済みのメールアドレスは大文字小文字が違っても409を返す() throws Exception {
		register(EMAIL, PASSWORD, PASSWORD).andExpect(status().isCreated());

		register("TEST-USER@example.com", PASSWORD, PASSWORD)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errors[0].field").value("email"))
				.andExpect(jsonPath("$.errors[0].message").value("このメールアドレスは既に登録されています。"));
	}

	// ---- API-02 ログイン / API-04 ログインユーザー取得 ----

	@Test
	void ログインに成功するとユーザー情報を返し_同じセッションでmeを取得できる() throws Exception {
		register(EMAIL, PASSWORD, PASSWORD);

		MockHttpSession session = login("Test-User@Example.com", PASSWORD);

		mockMvc.perform(get("/api/auth/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(EMAIL));
	}

	@Test
	void パスワード誤りと未登録のメールアドレスは同じ401を返す() throws Exception {
		register(EMAIL, PASSWORD, PASSWORD);
		String message = "メールアドレスまたはパスワードが正しくありません。";

		postJson("/api/auth/login", loginJson(EMAIL, "wrong-password1"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(message));
		postJson("/api/auth/login", loginJson("nobody@example.com", PASSWORD))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(message));
	}

	@Test
	void 未ログインでmeを呼ぶと401を返す() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized());
	}

	// ---- API-03 ログアウト ----

	@Test
	void ログアウトすると204を返し_以降はmeが401になる() throws Exception {
		register(EMAIL, PASSWORD, PASSWORD);
		MockHttpSession session = login(EMAIL, PASSWORD);

		mockMvc.perform(post("/api/auth/logout").session(session).with(csrf()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/auth/me").session(session))
				.andExpect(status().isUnauthorized());
	}

	// ---- CSRF対策（API設計書 1.3節） ----

	@Test
	void CSRFトークンが無いPOSTは403を返す() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginJson(EMAIL, PASSWORD)))
				.andExpect(status().isForbidden());
	}

	// ---- ヘルパー ----

	private ResultActions register(String email, String password,
			String passwordConfirm) throws Exception {
		String json = """
				{"email": "%s", "password": "%s", "passwordConfirm": "%s"}
				""".formatted(email, password, passwordConfirm);
		return postJson("/api/auth/register", json);
	}

	private MockHttpSession login(String email, String password) throws Exception {
		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(post("/api/auth/login").session(session).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginJson(email, password)))
				.andExpect(status().isOk());
		return session;
	}

	private ResultActions postJson(String url, String json) throws Exception {
		return mockMvc.perform(post(url).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	private static String loginJson(String email, String password) {
		return """
				{"email": "%s", "password": "%s"}
				""".formatted(email, password);
	}
}
