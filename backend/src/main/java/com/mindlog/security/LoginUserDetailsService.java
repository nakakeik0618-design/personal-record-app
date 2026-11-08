package com.mindlog.security;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mindlog.repository.UserRepository;

/**
 * ログイン時に、メールアドレスからユーザーを探す（Spring Security から呼ばれる）。
 */
@Service
public class LoginUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public LoginUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
				.map(LoginUser::new)
				.orElseThrow(() -> new UsernameNotFoundException("user not found"));
	}
}
