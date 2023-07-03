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

import com.studysquad.global.error.exception.DuplicateEmailException;
import com.studysquad.global.error.exception.DuplicateNicknameException;
import com.studysquad.global.error.exception.InvalidSigningInformation;
import com.studysquad.global.security.AccessToken;
import com.studysquad.global.security.JwtProvider;
import com.studysquad.global.security.RefreshToken;
import com.studysquad.global.security.Token;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.JoinRequestDto;
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
		User user = createUser();
		Token token = createToken();

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
		User user = createUser();

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		LoginRequestDto loginRequestDto = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("wrong-password")
			.build();

		assertThatThrownBy(() -> authService.login(loginRequestDto))
			.isInstanceOf(InvalidSigningInformation.class);
	}

	@Test
	@DisplayName("회원가입 성공")
	void successfulJoin() {
		JoinRequestDto joinRequestDto = JoinRequestDto.builder()
			.email("aaa@aaa.com")
			.password("password")
			.nickname("nickname")
			.build();

		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(userRepository.existsByNickname(anyString())).thenReturn(false);

		authService.join(joinRequestDto);

		verify(userRepository, times(1)).save(any());
	}

	@Test
	@DisplayName("중복된 이메일로 가입")
	void duplicateEmailFailJoin() {
		createUser();

		JoinRequestDto joinRequestDto = JoinRequestDto.builder()
			.email("aaa@aaa.com")
			.password("password")
			.nickname("nickname")
			.build();

		when(userRepository.existsByEmail(joinRequestDto.getEmail())).thenReturn(true);

		assertThatThrownBy(() -> authService.join(joinRequestDto))
			.isInstanceOf(DuplicateEmailException.class);
	}

	@Test
	@DisplayName("중복된 닉네임으로 가입")
	void duplicateNicknameFailJoin() {
		createUser();

		JoinRequestDto joinRequestDto = JoinRequestDto.builder()
			.email("otherEmail@aaa.com")
			.password("password")
			.nickname("nickname")
			.build();

		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(userRepository.existsByNickname(joinRequestDto.getNickname())).thenReturn(true);

		assertThatThrownBy(() -> authService.join(joinRequestDto))
			.isInstanceOf(DuplicateNicknameException.class);
	}

	private User createUser() {
		return User.builder()
			.email("aaa@aaa.com")
			.password(passwordEncoder.encode("password"))
			.nickname("nickname")
			.build();
	}

	private Token createToken() {
		AccessToken accessToken = AccessToken.builder()
			.header("Authorization")
			.data("accessToken")
			.build();
		RefreshToken refreshToken = RefreshToken.builder()
			.header("Authorization-refresh")
			.data("refreshToken")
			.build();
		return Token.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}
}
