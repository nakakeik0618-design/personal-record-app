package com.mindlog.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mindlog.dto.RegisterRequest;
import com.mindlog.entity.User;
import com.mindlog.exception.FieldValidationException;
import com.mindlog.repository.UserRepository;

@Service
public class UserService {

	static final String DUPLICATE_EMAIL = "このメールアドレスは既に登録されています。";
	static final String PASSWORD_MISMATCH = "パスワードが一致しません。";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * ユーザーを登録する（FR-002）。
	 */
	@Transactional
	public void register(RegisterRequest request) {
		if (!request.password().equals(request.passwordConfirm())) {
			throw new FieldValidationException(HttpStatus.BAD_REQUEST, "passwordConfirm", PASSWORD_MISMATCH);
		}

		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (userRepository.existsByEmail(email)) {
			throw new FieldValidationException(HttpStatus.CONFLICT, "email", DUPLICATE_EMAIL);
		}

		try {
			userRepository.saveAndFlush(new User(email, passwordEncoder.encode(request.password())));
		} catch (DataIntegrityViolationException e) {
			// 同時に同じメールアドレスで登録された場合は、DBの一意制約で検出される
			throw new FieldValidationException(HttpStatus.CONFLICT, "email", DUPLICATE_EMAIL);
		}
	}
}
