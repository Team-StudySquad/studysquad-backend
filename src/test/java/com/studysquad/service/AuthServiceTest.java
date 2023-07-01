package com.studysquad.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.studysquad.global.error.exception.InvalidSigningInformation;
import com.studysquad.global.security.AccessToken;
import com.studysquad.global.security.JwtProvider;
import com.studysquad.global.security.RefreshToken;
import com.studysquad.global.security.Token;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginRequestDto;
import com.studysquad.user.repository.UserRepository;
import com.studysquad.user.service.AuthService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@Mock
	UserRepository userRepository;
	@Mock
	JwtProvider jwtProvider;
	AuthService authService;
	PasswordEncoder passwordEncoder;

	@BeforeEach
	void init() {
		passwordEncoder = new BCryptPasswordEncoder();
		authService = new AuthService(userRepository, jwtProvider, passwordEncoder);
	}

	@Test
	@DisplayName("로그인 성공")
	void successfulLogin() {
		User user = generatedUser();
		Token token = generatedToken(user.getEmail());

		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(jwtProvider.createToken(user.getEmail())).thenReturn(token);

		LoginRequestDto loginRequestDto = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("password")
			.build();

		Token returnedToken = authService.login(loginRequestDto);

		assertThat(returnedToken).isNotNull();
		assertThat(returnedToken.getAccessToken()).isEqualTo(token.getAccessToken());
		assertThat(returnedToken.getRefreshToken()).isEqualTo(token.getRefreshToken());
	}

	@Test
	@DisplayName("존재하지 않는 이메일로 로그인 시도")
	void nonExistentEmailLoginFailure() {
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

		LoginRequestDto loginRequestDto = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("password")
			.build();
		assertThatThrownBy(() -> authService.login(loginRequestDto))
			.isInstanceOf(InvalidSigningInformation.class);
	}

	@Test
	@DisplayName("일치하지 않는 비밀번호로 로그인 시도")
	void passwordMismatchLoginFailure() {
		User user = generatedUser();

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		LoginRequestDto loginRequestDto = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("wrong-password")
			.build();

		assertThatThrownBy(() -> authService.login(loginRequestDto))
			.isInstanceOf(InvalidSigningInformation.class);
	}

	private User generatedUser() {
		return User.builder()
			.email("aaa@aaa.com")
			.password(passwordEncoder.encode("password"))
			.build();
	}

	private Token generatedToken(String email) {
		AccessToken accessToken = AccessToken.builder()
			.header("Authorization")
			.data("accessToken")
			.build();
		RefreshToken refreshToken = RefreshToken.builder()
			.header("Authorization-refresh")
			.expirationPeriod(10)
			.data("refreshToken")
			.build();
		return Token.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}
}
