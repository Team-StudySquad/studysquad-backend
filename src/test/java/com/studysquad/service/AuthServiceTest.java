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
import com.studysquad.global.error.exception.InvalidTokenException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.global.security.AccessToken;
import com.studysquad.global.security.JwtProvider;
import com.studysquad.global.security.RefreshToken;
import com.studysquad.global.security.Token;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.JoinRequestDto;
import com.studysquad.user.dto.LoginRequestDto;
import com.studysquad.user.dto.LoginUser;
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

	@Test
	@DisplayName("토큰 재발급 성공")
	void successfulReissue() {
		User user = createUser();
		Token token = createToken();
		RefreshToken refreshToken = RefreshToken.builder()
			.header("Authorization-refresh")
			.data("validRefreshToken")
			.build();
		String refreshTokenValue = refreshToken.getData();

		when(userRepository.findByRefreshToken(refreshTokenValue)).thenReturn(Optional.of(user));
		when(jwtProvider.isTokenValid(refreshTokenValue)).thenReturn(true);
		when(jwtProvider.createToken(user.getEmail())).thenReturn(token);

		Token returnedToken = authService.reissue(refreshToken);

		assertThat(returnedToken.getAccessToken()).isNotNull();
		assertThat(returnedToken.getRefreshToken()).isNotNull();
	}

	@Test
	@DisplayName("올바르지 않은 RefreshToken으로 요청")
	void failReissueWithInvalidRefreshToken() {
		RefreshToken invalidToken = RefreshToken.builder()
			.header("Authorization-refresh")
			.data("invalidRefreshToken")
			.build();

		when(jwtProvider.isTokenValid(invalidToken.getData())).thenReturn(false);

		assertThatThrownBy(() -> authService.reissue(invalidToken))
			.isInstanceOf(InvalidTokenException.class);
	}

	@Test
	@DisplayName("RefreshToken으로 사용자 정보를 찾을 수 없음")
	void failReissueWithUserNotFound() {
		RefreshToken refreshToken = RefreshToken.builder()
			.header("Authorization-refresh")
			.data("validRefreshToken")
			.build();
		String refreshTokenValue = refreshToken.getData();

		when(userRepository.findByRefreshToken(refreshTokenValue)).thenReturn(Optional.empty());
		when(jwtProvider.isTokenValid(refreshTokenValue)).thenReturn(true);

		assertThatThrownBy(() -> authService.reissue(refreshToken))
			.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	@DisplayName("로그아웃 성공")
	void successLogout() {
		User user = createUser();
		LoginUser loginUser = LoginUser.builder()
			.email(user.getEmail())
			.role(Role.USER)
			.build();
		user.updateRefreshToken(createRefreshToken().getData());

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		authService.logout(loginUser);

		assertThat(user.getRefreshToken()).isNull();
	}

	@Test
	@DisplayName("LoginUser 정보로 사용자를 찾을 수 없음")
	void failLogoutNotFoundUserWithLoginUser() {
		LoginUser loginUser = LoginUser.builder()
			.email("wrongEmail")
			.role(Role.USER)
			.build();

		when(userRepository.findByEmail(loginUser.getEmail())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.logout(loginUser))
			.isInstanceOf(UserNotFoundException.class);
	}

	private User createUser() {
		return User.builder()
			.email("aaa@aaa.com")
			.password(passwordEncoder.encode("password"))
			.nickname("nickname")
			.build();
	}

	private Token createToken() {
		return Token.builder()
			.accessToken(createAccessToken())
			.refreshToken(createRefreshToken())
			.build();
	}

	private AccessToken createAccessToken() {
		return AccessToken.builder()
			.header("Authorization")
			.data("accessToken")
			.build();
	}

	private RefreshToken createRefreshToken() {
		return RefreshToken.builder()
			.header("Authorization-refresh")
			.data("refreshToken")
			.build();
	}
}
