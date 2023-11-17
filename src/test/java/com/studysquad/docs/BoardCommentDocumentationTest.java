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
import com.studysquad.boardcomment.controller.BoardCommentController;
import com.studysquad.boardcomment.dto.BoardCommentCreateDto;
import com.studysquad.boardcomment.dto.BoardCommentEditDto;
import com.studysquad.boardcomment.dto.BoardCommentResponse;
import com.studysquad.boardcomment.service.BoardCommentService;
import com.studysquad.docs.util.TokenFactory;
import com.studysquad.global.security.Token;

@WebMvcTest(BoardCommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class BoardCommentDocumentationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private BoardCommentService boardCommentService;

	@Test
	@DisplayName("게시글 댓글 조회")
	void successGetBoardComments() throws Exception {

		Long boardId = 1L;

		List<BoardCommentResponse> response = LongStream.range(1, 3)
			.mapToObj(i -> BoardCommentResponse.builder()
				.boardCommentId(i)
				.boardCommentContent("게시글 댓글 내용" + i)
				.creator("user" + i)
				.createAt(LocalDateTime.now())
				.build())
			.collect(Collectors.toList());

		when(boardCommentService.getBoardComments(any(Long.class)))
			.thenReturn(response);

		mockMvc.perform(get("/api/board/{boardId}/boardcomments", boardId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-board-comments",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("boardId").description("게시글 아이디")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data[].boardCommentId").description("게시글 댓글 아이디"),
					fieldWithPath("data[].boardCommentContent").description("게시글 댓글 내용"),
					fieldWithPath("data[].creator").description("게시글 댓글 작성자"),
					fieldWithPath("data[].createAt").description("게시글 댓글 작성시간")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 댓글 생성")
	void successCreateBoardComment() throws Exception {

		Long boardId = 1L;

		Token token = TokenFactory.createToken();

		BoardCommentCreateDto request = BoardCommentCreateDto.builder()
			.boardCommentContent("게시글 댓글 내용")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/board/{boardId}/boardcomment", boardId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andDo(print())
			.andDo(document("create-board-comment",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("boardId").description("게시글 아이디")
				),
				requestFields(
					fieldWithPath("boardCommentContent").description("게시글 댓글 내용")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 댓글 수정")
	void successEditBoardComment() throws Exception {

		Long boardId = 1L;
		Long boardCommentId = 1L;

		Token token = TokenFactory.createToken();

		BoardCommentEditDto request = BoardCommentEditDto.builder()
			.boardCommentContent("수정될 게시글 댓글 내용")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/board/{boardId}/boardcomment/{boardCommentId}", boardId, boardCommentId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("edit-board-comment",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("boardId").description("게시글 아이디"),
					parameterWithName("boardCommentId").description("게시글 댓글 아이디")
				),
				requestFields(
					fieldWithPath("boardCommentContent").description("수정될 게시글 댓글 내용")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 댓글 삭제")
	void successDeleteBoardComment() throws Exception {

		Long boardId = 1L;
		Long boardCommentId = 1L;

		Token token = TokenFactory.createToken();

		mockMvc.perform(delete("/api/board/{boardId}/boardcomment/{boardCommentId}", boardId, boardCommentId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData()))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("delete-board-comment",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("boardId").description("게시글 아이디"),
					parameterWithName("boardCommentId").description("게시글 댓글 아이디")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

}
