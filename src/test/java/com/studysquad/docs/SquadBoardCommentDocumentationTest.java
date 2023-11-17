package com.studysquad.docs;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
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
import com.studysquad.sqaudboardcomment.controller.SquadBoardCommentController;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentCreateDto;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentEditDto;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentResponseDto;
import com.studysquad.sqaudboardcomment.service.SquadBoardCommentService;
import com.studysquad.user.dto.LoginUser;

@WebMvcTest(SquadBoardCommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class SquadBoardCommentDocumentationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private SquadBoardCommentService squadBoardCommentService;

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 전체 조회")
	void successGetSquadBoardComments() throws Exception {

		Long squadId = 1L;
		Long squadBoardId = 1L;

		Token token = TokenFactory.createToken();

		List<SquadBoardCommentResponseDto> response = LongStream.range(0, 3)
			.mapToObj(i -> SquadBoardCommentResponseDto.builder()
				.squadBoardCommentId(i)
				.commentContent("스쿼드 게시글 댓글 내용")
				.creator("user" + i)
				.createAt(LocalDateTime.now())
				.build())
			.collect(Collectors.toList());

		when(squadBoardCommentService.getSquadBoardComments(any(LoginUser.class), any(Long.class), any(Long.class)))
			.thenReturn(response);

		mockMvc.perform(
				get("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomments",
					squadId, squadBoardId)
					.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-squad-board-comments",
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
					fieldWithPath("data[].squadBoardCommentId").description("스쿼드 게시글 댓글 아이디"),
					fieldWithPath("data[].squadBoardCommentContent").description("스쿼드 게시글 댓글 내용"),
					fieldWithPath("data[].creator").description("스쿼드 게시글 댓글 작성자"),
					fieldWithPath("data[].createAt").description("스쿼드 게시글 댓글 생성 시간")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 생성")
	void successCreateSquadBoardComment() throws Exception {

		Long squadId = 1L;
		Long squadBoardId = 1L;

		Token token = TokenFactory.createToken();

		SquadBoardCommentCreateDto request = SquadBoardCommentCreateDto.builder()
			.squadBoardCommentContent("스쿼드 게시글 댓글 내용")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment", squadId, squadBoardId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andDo(print())
			.andDo(document("create-squad-board-comment",
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
					fieldWithPath("squadBoardCommentContent").description("스쿼드 게시글 댓글 내용")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 수정")
	void successEditSquadBoardComment() throws Exception {

		Long squadId = 1L;
		Long squadBoardId = 1L;
		Long squadBoardCommentId = 1L;

		Token token = TokenFactory.createToken();

		SquadBoardCommentEditDto request = SquadBoardCommentEditDto.builder()
			.commentContent("수정할 스쿼드 게시글 댓글 내용")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(
				patch("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}", squadId,
					squadBoardId, squadBoardCommentId)
					.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("edit-squad-board-comment",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디"),
					parameterWithName("squadBoardId").description("스쿼드 게시글 아이디"),
					parameterWithName("squadBoardCommentId").description("스쿼드 게시글 댓글 아이디")
				),
				requestFields(
					fieldWithPath("squadBoardCommentContent").description("수정할 스쿼드 게시글 댓글 내용")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 삭제")
	void successDeleteSquadBoardComment() throws Exception {

		Long squadId = 1L;
		Long squadBoardId = 1L;
		Long squadBoardCommentId = 1L;

		Token token = TokenFactory.createToken();

		mockMvc.perform(
				delete("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}", squadId,
					squadBoardId, squadBoardCommentId)
					.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("delete-squad-board-comment",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디"),
					parameterWithName("squadBoardId").description("스쿼드 게시글 아이디"),
					parameterWithName("squadBoardCommentId").description("스쿼드 게시글 댓글 아이디")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}
}
