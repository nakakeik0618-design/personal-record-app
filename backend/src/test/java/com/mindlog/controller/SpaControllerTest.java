package com.mindlog.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * React の画面の URL に直接アクセスしたときに index.html へ転送されることのテスト。
 */
@SpringBootTest
@AutoConfigureMockMvc
class SpaControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void 画面のURLはログインしていなくてもindex_htmlへ転送される() throws Exception {
		for (String url : new String[] { "/login", "/register", "/records", "/records/new/diary", "/records/10/edit" }) {
			mockMvc.perform(get(url))
					.andExpect(status().isOk())
					.andExpect(forwardedUrl("/index.html"));
		}
	}

	@Test
	void APIは転送されず_未ログインなら401のまま() throws Exception {
		mockMvc.perform(get("/api/records"))
				.andExpect(status().isUnauthorized());
	}
}
