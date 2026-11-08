package com.mindlog.dto;

/**
 * ログインのリクエスト（API-02）。
 * 入力チェックで失敗理由を返すとヒントになるため、チェックは行わず認証失敗として扱う（NFR-002）。
 */
public record LoginRequest(String email, String password) {
}
