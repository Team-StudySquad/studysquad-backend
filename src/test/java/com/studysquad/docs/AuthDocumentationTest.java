package com.studysquad.docs;

import static com.studysquad.docs.util.RequestCookieSnippet.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import javax.servlet.http.Cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.docs.util.TokenFactory;
import com.studysquad.global.security.RefreshToken;
import com.studysquad.global.security.Token;
import com.studysquad.user.controller.AuthController;
import com.studysquad.user.dto.JoinRequestDto;
import com.studysquad.user.dto.LoginRequestDto;
import com.studysquad.user.service.AuthService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class AuthDocumentationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private AuthService authService;

	@Test
	@DisplayName("로그인 성공")
	void successLogin() throws Exception {
		LoginRequestDto loginRequest = LoginRequestDto.builder()
			.email("aaa@aaa.com")
			.password("password")
			.build();

		when(authService.login(any(LoginRequestDto.class)))
			.thenReturn(TokenFactory.createToken());

		String json = objectMapper.writeValueAsString(loginRequest);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("user-login",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
					fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("응답 메세지")
				),
				responseHeaders(
					headerWithName("Authorization").description("엑세스 토큰"),
					headerWithName("Set-Cookie").description("리프레시 토큰")
				)));
	}

	@Test
	@DisplayName("회원가입 성공")
	void successJoin() throws Exception {
		JoinRequestDto joinRequest = JoinRequestDto.builder()
			.email("aaa@aaa.com")
			.password("1234")
			.nickname("userA")
			.build();

		String json = objectMapper.writeValueAsString(joinRequest);

		mockMvc.perform(post("/api/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("user-join",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
					fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
					fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("응답 메세지")
				)));
	}

	@Test
	@DisplayName("토큰 재발급 성공")
	void successReIssue() throws Exception {
		Cookie cookie = new Cookie("Authorization-refresh", "refreshToken.Before.Reissue");

		when(authService.reissue(any(RefreshToken.class)))
			.thenReturn(TokenFactory.createToken());

		mockMvc.perform(post("/api/reissue")
				.cookie(cookie))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("user-reissue",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaderCookies(
					cookieWithName("Authorization-refresh").description("재발급 되기 전 리프레시 토큰")
				),
				responseHeaders(
					headerWithName("Authorization").description("재발급된 액세스 토큰"),
					headerWithName("Set-Cookie").description("재발급된 리프레시 토큰")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

	@Test
	@DisplayName("로그아웃 성공")
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	void successLogout() throws Exception {
		Token token = TokenFactory.createToken();
		Cookie cookie = new Cookie(token.getRefreshToken().getHeader(), token.getRefreshToken().getData());

		mockMvc.perform(post("/api/logout")
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.cookie(cookie))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("user-logout",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				requestHeaderCookies(
					cookieWithName("Authorization-refresh").description("리프래시 토큰")
				),
				responseHeaders(
					headerWithName("Set-Cookie").description("리프래시 토큰 제거")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}
}
