package com.studysquad.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studysquad.global.error.exception.DuplicateEmailException;
import com.studysquad.global.error.exception.DuplicateNicknameException;
import com.studysquad.global.error.exception.InvalidSigningInformation;
import com.studysquad.global.security.JwtProvider;
import com.studysquad.global.security.Token;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.JoinRequestDto;
import com.studysquad.user.dto.LoginRequestDto;
import com.studysquad.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final JwtProvider jwtProvider;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public Token login(LoginRequestDto loginRequestDto) {
		String email = loginRequestDto.getEmail();
		String password = loginRequestDto.getPassword();

		User user = userRepository.findByEmail(email)
			.orElseThrow(InvalidSigningInformation::new);

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new InvalidSigningInformation();
		}

		Token token = jwtProvider.createToken(email);

		user.addRefreshToken(token.getRefreshToken().getData());

		return token;
	}

	@Transactional
	public void join(JoinRequestDto joinRequestDto) {
		if (userRepository.existsByEmail(joinRequestDto.getEmail())) {
			throw new DuplicateEmailException();
		}
		if (userRepository.existsByNickname(joinRequestDto.getNickname())) {
			throw new DuplicateNicknameException();
		}
		joinRequestDto.passwordEncryption(passwordEncoder);

		userRepository.save(joinRequestDto.toEntity());
	}
}
