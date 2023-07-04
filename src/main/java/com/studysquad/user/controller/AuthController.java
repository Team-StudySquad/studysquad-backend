package com.studysquad.user.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.studysquad.global.common.SuccessResponse;
import com.studysquad.global.security.AccessToken;
import com.studysquad.global.security.RefreshToken;
import com.studysquad.global.security.Token;
import com.studysquad.user.dto.JoinRequestDto;
import com.studysquad.user.dto.LoginRequestDto;
import com.studysquad.user.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/api/login")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Void> signIn(@RequestBody @Valid LoginRequestDto loginDto, HttpServletResponse response) {
		Token token = authService.login(loginDto);

		setAccessToken(response, token.getAccessToken());
		setRefreshToken(response, token.getRefreshToken());

		return SuccessResponse.<Void>builder()
			.status(200)
			.message("로그인 성공")
			.build();
	}

	@PostMapping("/api/join")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Void> signUp(@RequestBody @Valid JoinRequestDto joinDto) {
		authService.join(joinDto);

		return SuccessResponse.<Void>builder()
			.status(200)
			.message("회원 가입 성공")
			.build();
	}

	@PostMapping("/api/reissue")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Void> reissueToken(RefreshToken refreshToken, HttpServletResponse response) {
		Token token = authService.reissue(refreshToken);

		setAccessToken(response, token.getAccessToken());
		setRefreshToken(response, token.getRefreshToken());

		return SuccessResponse.<Void>builder()
			.status(200)
			.message("토큰 재발급 성공")
			.build();
	}

	private void setAccessToken(HttpServletResponse response, AccessToken accessToken) {
		setHeader(response, accessToken.getHeader(), accessToken.getData());
	}

	private void setRefreshToken(HttpServletResponse response, RefreshToken refreshToken) {
		Cookie cookie = createCookie(refreshToken.getHeader(), refreshToken.getData());
		response.addCookie(cookie);
	}

	private void setHeader(HttpServletResponse response, String header, String data) {
		response.setHeader(header, data);
	}

	private Cookie createCookie(String name, String value) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(RefreshToken.EXPIRATION_PERIOD);
		cookie.setHttpOnly(true);
		return cookie;
	}
}
