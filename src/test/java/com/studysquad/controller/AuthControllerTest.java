package com.studysquad.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.global.security.JwtProvider;
import com.studysquad.global.security.RefreshToken;
import com.studysquad.global.security.Token;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.JoinRequestDto;
import com.studysquad.user.dto.LoginRequestDto;
import com.studysquad.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	UserRepository userRepository;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	JwtProvider jwtProvider;

	@BeforeEach
	void clean() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("로그인 성공")
	void successfulLogin() throws Exception {
		userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@Test
	@DisplayName("로그인 성공 후 헤더에 토큰 발급")
	void afterSuccessLoginIssueTokenForHeader() throws Exception {
		userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(header().exists("Authorization"))
			.andDo(print());
	}

	@Test
	@DisplayName("로그인 성공 후 쿠키 발급")
	void afterSuccessLoginIssueCookie() throws Exception {
		userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(cookie().exists("Authorization-refresh"))
			.andDo(print());
	}

	@Test
	@Transactional
	@DisplayName("로그인 성공 후 RefreshToken 생성")
	void afterSuccessLoginCreateRefreshTokenForUser() throws Exception {
		User user = userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());

		assertThat(user.getRefreshToken()).isNotNull();
	}

	@Test
	@DisplayName("로그인 성공 시 응답 바디를 리턴해준다.")
	void afterSuccessLoginReturnResponseBody() throws Exception {
		userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(jsonPath("$.status").value("200"))
			.andExpect(jsonPath("$.message").value("로그인 성공"))
			.andExpect(jsonPath("$.data").isEmpty())
			.andDo(print());
	}

	@Test
	@DisplayName("존재하지 않는 이메일로 로그인 시도")
	void failLoginWithNoneExistUsername() throws Exception {
		userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("wrongPassword@aaa.com")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isUnauthorized())
			.andDo(print());
	}

	@Test
	@DisplayName("존재하지 않는 이메일로 로그인시 응답 바디를 리턴")
	void failLoginWithNoneExistUsernameReturnResponseBody() throws Exception {
		userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("wrongPassword@aaa.com")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.status").value("401"))
			.andExpect(jsonPath("$.message").value("아이디/비밀번호가 일치하지 않습니다"))
			.andExpect(jsonPath("$.validation").isEmpty())
			.andDo(print());

	}

	@Test
	@DisplayName("일치하지 않는 비밀번호로 로그인 시도")
	void failLoginWithIncorrectPassword() throws Exception {
		userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("wrongPassword")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isUnauthorized())
			.andDo(print());
	}

	@Test
	@DisplayName("일치하지 않는 비밀번호로 로그인 시도시 응답 바디 리턴")
	void failLoginWithIncorrectPasswordReturnResponseBody() throws Exception {
		userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("wrongPassword")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.status").value("401"))
			.andExpect(jsonPath("$.message").value("아이디/비밀번호가 일치하지 않습니다"))
			.andExpect(jsonPath("$.validation").isEmpty())
			.andDo(print());
	}

	@Test
	@DisplayName("이메일에 빈 문자열로 로그인 시도시 응답 바디 리턴")
	void failLoginWithEmptyEmailReturnResponseBody() throws Exception {
		userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
			.andExpect(jsonPath("$.validation.email").value("이메일을 입력해주세요"))
			.andDo(print());
	}

	@Test
	@DisplayName("비밀번호에 빈 문자열로 로그인 시도시 응답 바디 리턴")
	void failLoginWithEmptyPasswordReturnResponseBody() throws Exception {
		userRepository.save(createUser());

		LoginRequestDto login = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("")
			.build();

		String json = objectMapper.writeValueAsString(login);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
			.andExpect(jsonPath("$.validation.password").value("비밀번호를 입력해주세요"))
			.andDo(print());
	}

	@Test
	@DisplayName("회원가입 성공")
	void successfulJoin() throws Exception {
		JoinRequestDto join = JoinRequestDto.builder()
			.email("aaa@aaa.com")
			.password("1234")
			.nickname("nickname")
			.build();

		String json = objectMapper.writeValueAsString(join);

		mockMvc.perform(post("/api/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@Test
	@DisplayName("회원가입 성공에 대한 응답바디 리턴")
	void successfulJoinReturnResponseBody() throws Exception {
		JoinRequestDto join = JoinRequestDto.builder()
			.email("aaa@aaa.com")
			.password("1234")
			.nickname("nickname")
			.build();

		String json = objectMapper.writeValueAsString(join);

		mockMvc.perform(post("/api/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("200"))
			.andExpect(jsonPath("$.message").value("회원 가입 성공"))
			.andExpect(jsonPath("$.data").isEmpty())
			.andDo(print());
	}

	@Test
	@DisplayName("중복된 이메일로 가입 시 응답 바디 리턴")
	void failJoinDuplicateEmailReturnResponseBody() throws Exception {
		userRepository.save(createUser());

		JoinRequestDto join = JoinRequestDto.builder()
			.email("aaa@aaa.com")
			.password("password")
			.nickname("nickname")
			.build();

		String json = objectMapper.writeValueAsString(join);

		mockMvc.perform(post("/api/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value("409"))
			.andExpect(jsonPath("$.message").value("중복된 이메일 입니다"))
			.andExpect(jsonPath("$.validation").isEmpty())
			.andDo(print());
	}

	@Test
	@DisplayName("중복된 닉네임으로 가입 시 응답 바디 리턴")
	void failJoinDuplicateNicknameReturnResponseBody() throws Exception {
		userRepository.save(createUser());

		JoinRequestDto join = JoinRequestDto.builder()
			.email("otherEmail@aaa.com")
			.password("password")
			.nickname("nickname")
			.build();

		String json = objectMapper.writeValueAsString(join);

		mockMvc.perform(post("/api/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value("409"))
			.andExpect(jsonPath("$.message").value("중복된 닉네임 입니다"))
			.andExpect(jsonPath("$.validation").isEmpty())
			.andDo(print());
	}

	@Test
	@DisplayName("이메일에 빈 문자열로 가입 시 응답 바디 리턴")
	void failJoinEmptyEmailReturnResponseBody() throws Exception {
		JoinRequestDto join = JoinRequestDto.builder()
			.email("")
			.password("1234")
			.nickname("nickname")
			.build();

		String json = objectMapper.writeValueAsString(join);

		mockMvc.perform(post("/api/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
			.andExpect(jsonPath("$.validation.email").value("이메일을 입력해주세요"))
			.andDo(print());
	}

	@Test
	@DisplayName("비밀번호에 빈 문자열로 가입 시 응답 바디 리턴")
	void failJoinEmptyPasswordReturnResponseBody() throws Exception {
		JoinRequestDto join = JoinRequestDto.builder()
			.email("aaa@aaa.com")
			.password("")
			.nickname("nickname")
			.build();

		String json = objectMapper.writeValueAsString(join);

		mockMvc.perform(post("/api/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
			.andExpect(jsonPath("$.validation.password").value("비밀번호를 입력해주세요"))
			.andDo(print());
	}

	@Test
	@DisplayName("닉네임에 빈 문자열로 가입 시 응답 바디 리턴")
	void failJoinEmptyNicknameReturnResponseBody() throws Exception {
		JoinRequestDto join = JoinRequestDto.builder()
			.email("aaa@aaa.com")
			.password("1234")
			.nickname("")
			.build();

		String json = objectMapper.writeValueAsString(join);

		mockMvc.perform(post("/api/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
			.andExpect(jsonPath("$.validation.nickname").value("닉네임을 입력해주세요"))
			.andDo(print());
	}

	@Test
	@DisplayName("토큰 재발급 성공")
	@Transactional
	void successfulReissueToken() throws Exception {
		User user = userRepository.save(createUser());
		Token token = jwtProvider.createToken(user.getEmail());
		RefreshToken refreshToken = token.getRefreshToken();

		user.updateRefreshToken(refreshToken.getData());

		Cookie requestCookie = new Cookie(refreshToken.getHeader(), refreshToken.getData());

		mockMvc.perform(post("/api/reissue")
				.cookie(requestCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("200"))
			.andExpect(jsonPath("$.message").value("토큰 재발급 성공"))
			.andExpect(header().exists(token.getAccessToken().getHeader()))
			.andExpect(cookie().exists(refreshToken.getHeader()))
			.andExpect(result -> {
				MockHttpServletResponse response = result.getResponse();
				Cookie[] cookies = response.getCookies();
				Optional<Cookie> myCookie = Arrays.stream(cookies)
					.filter(cookie -> cookie.getName().equals(refreshToken.getHeader()))
					.findFirst();

				assertThat(myCookie).isPresent();
				assertThat(myCookie.get().getValue()).isEqualTo(user.getRefreshToken());
			})
			.andDo(print());
	}

	@Test
	@DisplayName("유효하지 않은 토큰으로 요청시 응답 바디 리턴")
	void failReissueWrongTokenReturnResponseBody() throws Exception {
		RefreshToken wrongToken = RefreshToken.builder()
			.header(jwtProvider.getRefreshHeader())
			.data("wrongData")
			.build();
		Cookie requestCookie = new Cookie(wrongToken.getHeader(), wrongToken.getData());

		mockMvc.perform(post("/api/reissue")
				.cookie(requestCookie))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.status").value("401"))
			.andExpect(jsonPath("$.message").value("유효하지 않는 토큰입니다"))
			.andExpect(result -> {
				MockHttpServletResponse response = result.getResponse();
				Cookie[] cookies = response.getCookies();

				Optional<Cookie> myCookie = Arrays.stream(cookies)
					.filter(cookie -> cookie.getName().equals(jwtProvider.getRefreshHeader()))
					.findFirst();

				assertThat(myCookie).isEmpty();
			})
			.andDo(print());
	}

	@Test
	@DisplayName("존재하지 않는 사용자 정보를 가지고 요청")
	@Transactional
	void failReissueExistTokenReturnResponseBody() throws Exception {
		User user = userRepository.save(createUser());
		Token token = jwtProvider.createToken(user.getEmail());
		RefreshToken refreshToken = token.getRefreshToken();

		user.updateRefreshToken("refreshToken");

		Cookie requestCookie = new Cookie(refreshToken.getHeader(), refreshToken.getData());

		mockMvc.perform(post("/api/reissue")
				.cookie(requestCookie))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value("404"))
			.andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다"))
			.andExpect(result -> {
				MockHttpServletResponse response = result.getResponse();
				Cookie[] cookies = response.getCookies();

				Optional<Cookie> myCookie = Arrays.stream(cookies)
					.filter(cookie -> cookie.getName().equals(jwtProvider.getRefreshHeader()))
					.findFirst();

				assertThat(myCookie).isEmpty();
			})
			.andDo(print());
	}

	private User createUser() {
		return User.builder()
			.email("aaa@aaa.com")
			.password(passwordEncoder.encode("1234"))
			.nickname("nickname")
			.role(Role.USER)
			.build();
	}
}
