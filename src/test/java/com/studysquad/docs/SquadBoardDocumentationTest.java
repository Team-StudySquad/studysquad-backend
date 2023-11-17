package com.studysquad.docs;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.docs.util.TokenFactory;
import com.studysquad.global.security.Token;
import com.studysquad.squadboard.controller.SquadBoardController;
import com.studysquad.squadboard.dto.SquadBoardCreateDto;
import com.studysquad.squadboard.dto.SquadBoardEditDto;
import com.studysquad.squadboard.dto.SquadBoardResponseDto;
import com.studysquad.squadboard.service.SquadBoardService;
import com.studysquad.user.dto.LoginUser;

@WebMvcTest(SquadBoardController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class SquadBoardDocumentationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private SquadBoardService squadBoardService;

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 단건 조회")
	void successGetSquadBoard() throws Exception {
		Long squadId = 1L;
		Long squadBoardId = 1L;
		Token token = TokenFactory.createToken();

		SquadBoardResponseDto response = SquadBoardResponseDto.builder()
			.squadBoardId(squadBoardId)
			.missionSequence(1)
			.squadBoardTitle("스쿼드 게시글 제목")
			.squadBoardContent("스쿼드 게시글 내용")
			.creator("userA")
			.build();

		when(squadBoardService.getSquadBoard(any(LoginUser.class), any(Long.class), any(Long.class)))
			.thenReturn(response);

		mockMvc.perform(get("/api/squad/{squadId}/squadboard/{squadBoardId}", squadId, squadBoardId)
				.contentType(MediaType.APPLICATION_JSON)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData()))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-squad-board",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디"),
					parameterWithName("squadBoardId").description("스쿼드 게시글 아이디")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data.squadBoardId").description("스쿼드 게시글 아이디"),
					fieldWithPath("data.missionSequence").description("미션 순서"),
					fieldWithPath("data.squadBoardTitle").description("스쿼드 게시글 제목"),
					fieldWithPath("data.squadBoardContent").description("스쿼드 게시글 내용"),
					fieldWithPath("data.creator").description("스쿼드 게시글 작성자")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 전체 조회")
	void successGetSquadBoards() throws Exception {
		Long squadId = 1L;
		Token token = TokenFactory.createToken();

		List<SquadBoardResponseDto> response = LongStream.range(0, 3)
			.mapToObj(i -> SquadBoardResponseDto.builder()
				.squadBoardId(i + 1)
				.missionSequence((int)i)
				.squadBoardTitle("스쿼드 게시글 제목" + i)
				.squadBoardContent("스쿼드 게시글 내용" + i)
				.creator("user" + i)
				.build())
			.collect(Collectors.toList());

		when(squadBoardService.getSquadBoards(any(LoginUser.class), any(Long.class)))
			.thenReturn(response);

		mockMvc.perform(get("/api/squad/{squadId}/squadboards", squadId)
				.contentType(MediaType.APPLICATION_JSON)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData()))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-squad-boards",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data[].squadBoardId").description("스쿼드 아이디"),
					fieldWithPath("data[].missionSequence").description("미션 순서"),
					fieldWithPath("data[].squadBoardTitle").description("스쿼드 게시글 제목"),
					fieldWithPath("data[].squadBoardContent").description("스쿼드 게시글 내용"),
					fieldWithPath("data[].creator").description("스쿼드 게시글 작성자")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 생성")
	void successCreateSquadBoard() throws Exception {
		Long squadId = 1L;
		Token token = TokenFactory.createToken();

		SquadBoardCreateDto request = SquadBoardCreateDto.builder()
			.squadBoardTitle("스쿼드 게시글 제목")
			.squadBoardContent("스쿼드 게시글 내용")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/squadboard", squadId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andDo(print())
			.andDo(document("create-squad-board",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디")
				),
				requestFields(
					fieldWithPath("squadBoardTitle").description("스쿼드 게시글 제목"),
					fieldWithPath("squadBoardContent").description("스쿼드 게시글 내용")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 수정")
	void successEditSquadBoard() throws Exception {
		Long squadId = 1L;
		Long squadBoardId = 1L;
		Token token = TokenFactory.createToken();

		SquadBoardEditDto request = SquadBoardEditDto.builder()
			.squadBoardTitle("수정된 스쿼드 게시글 제목")
			.squadBoardContent("수정된 스쿼드 게시글 내용")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/squad/{squadId}/squadboard/{squadBoardId}", squadId, squadBoardId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("edit-squad-board",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디"),
					parameterWithName("squadBoardId").description("스쿼드 게시글 아이디")
				),
				requestFields(
					fieldWithPath("squadBoardTitle").description(" 수정할 스쿼드 게시글 이름"),
					fieldWithPath("squadBoardContent").description("수정할 스쿼드 게시글 내용")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

}
