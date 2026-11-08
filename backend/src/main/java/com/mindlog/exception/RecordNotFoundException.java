package com.mindlog.exception;

/**
 * 記録が存在しない、または他ユーザーの記録である（404。API設計書 1.5節）。
 */
public class RecordNotFoundException extends RuntimeException {
}
