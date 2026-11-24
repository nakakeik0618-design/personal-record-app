package com.mindlog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * React の画面の URL（API設計書 6節）に直接アクセスされたときに、React の index.html を返す。
 * /records/10 のような URL は実際のファイルが無いため、React（react-router）に画面の切り替えを任せる。
 * 開発時は Vite が同じことを行うため、この処理は React を Spring Boot から配信するとき（公開時）に使われる。
 */
@Controller
public class SpaController {

	@GetMapping({ "/login", "/register", "/records", "/records/**" })
	public String index() {
		// forward：ブラウザの URL は変えずに、サーバーの中で index.html の中身を返す
		return "forward:/index.html";
	}
}
