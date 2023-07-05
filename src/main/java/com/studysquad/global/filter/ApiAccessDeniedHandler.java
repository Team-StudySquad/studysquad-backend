package com.studysquad.global.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.global.common.FailResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {

		FailResponse error = FailResponse.builder()
			.status(HttpStatus.FORBIDDEN.value())
			.message("해당 권한이 없는 사용자 입니다.")
			.build();

		String json = objectMapper.writeValueAsString(error);

		setResponseProperties(response);
		writeJsonToResponse(response, json);
	}

	private void setResponseProperties(HttpServletResponse response) {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	}

	private void writeJsonToResponse(HttpServletResponse response, String json) throws IOException {
		response.getWriter().write(json);
	}
}
