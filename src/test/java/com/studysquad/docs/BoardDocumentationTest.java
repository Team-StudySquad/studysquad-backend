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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.board.controller.BoardController;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.request.BoardEdit;
import com.studysquad.board.request.BoardSearchCondition;
import com.studysquad.board.response.BoardResponse;
import com.studysquad.board.service.BoardService;
import com.studysquad.docs.util.TokenFactory;
import com.studysquad.global.security.Token;
import com.studysquad.user.dto.LoginUser;

@WebMvcTest(BoardController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class BoardDocumentationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private BoardService boardService;

	@Test
	@DisplayName("게시글 단건 조회")
	void successGetBoard() throws Exception {

		Long boardId = 1L;

		BoardResponse response = BoardResponse.builder()
			.boardId(boardId)
			.boardTitle("게시글 제목")
			.boardContent("게시글 내용")
			.categoryName("카테고리 이름")
			.missionTitle("미션 제목")
			.missionContent("미션 내용")
			.missionSequence(1)
			.squadName("스쿼드 이름")
			.creator("게시글 작성자")
			.build();

		when(boardService.getBoard(any(Long.class)))
			.thenReturn(response);

		mockMvc.perform(get("/api/board/{boardId}", boardId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-board",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("boardId").description("게시글 아이디")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data.boardId").description("게시글 아이디"),
					fieldWithPath("data.creator").description("게시글 작성자"),
					fieldWithPath("data.categoryName").description("카테고리 이름"),
					fieldWithPath("data.squadName").description("스쿼드 이름"),
					fieldWithPath("data.missionSequence").description("미션 순서"),
					fieldWithPath("data.missionTitle").description("미션 제목"),
					fieldWithPath("data.missionContent").description("미션 내용"),
					fieldWithPath("data.boardTitle").description("게시글 제목"),
					fieldWithPath("data.boardContent").description("게시글 내용")
				)));
	}

	@Test
	@DisplayName("게시글 페이징 조회")
	void successGetBoards() throws Exception {

		PageRequest page = PageRequest.of(0, 10);
		List<BoardResponse> responseData = LongStream.range(1, 3)
			.mapToObj(i -> BoardResponse.builder()
				.boardId(i)
				.boardTitle("게시글 제목" + i)
				.boardContent("게시글 내용" + i)
				.categoryName("JAVA")
				.missionTitle("미션 제목" + i)
				.missionContent("미션 내용" + i)
				.missionSequence(1)
				.squadName("스쿼드 이름" + i)
				.creator("게시글 작성자" + i)
				.build())
			.collect(Collectors.toList());

		Page<BoardResponse> response = new PageImpl<>(responseData, page, responseData.size());
		BoardSearchCondition cond = BoardSearchCondition.builder()
			.categoryName("JAVA")
			.build();

		when(boardService.getBoards(any(BoardSearchCondition.class), any(Pageable.class)))
			.thenReturn(response);

		mockMvc.perform(get("/api/boards")
				.accept(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page.getPageNumber()))
				.param("size", String.valueOf(page.getPageSize()))
				.param("categoryName", String.valueOf(cond.getCategoryName())))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-boards-with-page",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					parameterWithName("page").description("페이지 번호"),
					parameterWithName("size").description("페이지 사이즈"),
					parameterWithName("categoryName").description("카테고리 이름")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data.content[].boardId").description("게시글 아이디"),
					fieldWithPath("data.content[].creator").description("게시글 작성자"),
					fieldWithPath("data.content[].categoryName").description("카테고리 이름"),
					fieldWithPath("data.content[].squadName").description("스쿼드 이름"),
					fieldWithPath("data.content[].missionSequence").description("미션 순서"),
					fieldWithPath("data.content[].missionTitle").description("미션 제목"),
					fieldWithPath("data.content[].missionContent").description("미션 내용"),
					fieldWithPath("data.content[].boardTitle").description("게시글 제목"),
					fieldWithPath("data.content[].boardContent").description("게시글 내용"),
					fieldWithPath("data.pageable.sort.empty").description("정렬 존재 여부"),
					fieldWithPath("data.pageable.sort.sorted").description("정렬 여부"),
					fieldWithPath("data.pageable.sort.unsorted").description("정렬이 안되어 있는지 여부"),
					fieldWithPath("data.pageable.offset").description("페이지 오프셋"),
					fieldWithPath("data.pageable.pageNumber").description("페이지 번호"),
					fieldWithPath("data.pageable.pageSize").description("페이지 사이즈"),
					fieldWithPath("data.pageable.paged").description("페이징 된 결과인지 여부"),
					fieldWithPath("data.pageable.unpaged").description("페이징 하지 않은 결과인지 여부"),
					fieldWithPath("data.last").description("마지막 페이지 여부"),
					fieldWithPath("data.totalPages").description("전체 페이지 수"),
					fieldWithPath("data.totalElements").description("전체 요소 수"),
					fieldWithPath("data.size").description("한 페이지당 표시할 항목의 개수"),
					fieldWithPath("data.number").description("현재 페이지 번호"),
					fieldWithPath("data.sort.empty").description("정렬 존재 여부"),
					fieldWithPath("data.sort.sorted").description("정렬 여부"),
					fieldWithPath("data.sort.unsorted").description("정렬이 안되어 있는지 여부"),
					fieldWithPath("data.numberOfElements").description("현재 페이지에 포함된 요소"),
					fieldWithPath("data.first").description("첫 페이지 여부"),
					fieldWithPath("data.empty").description("데이터 존재 여부"))
			));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 작성 가능 조회")
	void successIsBoardAllowed() throws Exception {

		Long squadId = 1L;

		Token token = TokenFactory.createToken();

		when(boardService.isBoardAllowed(any(Long.class), any(LoginUser.class)))
			.thenReturn(true);

		mockMvc.perform(get("/api/squad/{squadId}/board/allowed", squadId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-board-allowed",
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
					fieldWithPath("data").description("작성 가능 여부")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 전체 게시글 조회")
	void successGetBoardsWithSquad() throws Exception {

		Long squadId = 1L;

		Token token = TokenFactory.createToken();
		List<BoardResponse> response = LongStream.range(1, 3)
			.mapToObj(i -> BoardResponse.builder()
				.boardId(i)
				.boardTitle("게시글 제목" + i)
				.boardContent("게시글 내용" + i)
				.categoryName("JAVA")
				.missionTitle("미션 제목" + i)
				.missionContent("미션 내용" + i)
				.missionSequence(1)
				.squadName("스쿼드 이름" + i)
				.creator("게시글 작성자" + i)
				.build())
			.collect(Collectors.toList());

		when(boardService.getBoardsWithSquad(any(Long.class), any(LoginUser.class)))
			.thenReturn(response);

		mockMvc.perform(get("/api/squad/{squadId}/boards", squadId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-boards-with-squad",
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
					fieldWithPath("data[].boardId").description("게시글 아이디"),
					fieldWithPath("data[].creator").description("게시글 작성자"),
					fieldWithPath("data[].categoryName").description("카테고리 이름"),
					fieldWithPath("data[].squadName").description("스쿼드 이름"),
					fieldWithPath("data[].missionSequence").description("미션 순서"),
					fieldWithPath("data[].missionTitle").description("미션 제목"),
					fieldWithPath("data[].missionContent").description("미션 내용"),
					fieldWithPath("data[].boardTitle").description("게시글 제목"),
					fieldWithPath("data[].boardContent").description("게시글 내용")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 작성")
	void successCreateBoard() throws Exception {

		Long squadId = 1L;

		Token token = TokenFactory.createToken();

		BoardCreate request = BoardCreate.builder()
			.title("게시글 이름")
			.content("게시글 내용")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/board", squadId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andDo(print())
			.andDo(document("create-board",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디")
				),
				requestFields(
					fieldWithPath("title").description("게시글 이름"),
					fieldWithPath("content").description("게시글 내용")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 수정")
	void successEditBoard() throws Exception {

		Long squadId = 1L;
		Long boardId = 1L;

		Token token = TokenFactory.createToken();

		BoardEdit request = BoardEdit.builder()
			.title("변경할 게시글 이름")
			.content("변경할 게시글 내용")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/squad/{squadId}/board/{boardId}", squadId, boardId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("edit-board",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디"),
					parameterWithName("boardId").description("게시글 아이디")
				),
				requestFields(
					fieldWithPath("title").description("변경할 게시글 이름"),
					fieldWithPath("content").description("변경할 게시글 내용")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 삭제")
	void successDeleteBoard() throws Exception {

		Long squadId = 1L;
		Long boardId = 1L;

		Token token = TokenFactory.createToken();

		mockMvc.perform(delete("/api/squad/{squadId}/board/{boardId}", squadId, boardId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("delete-board",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디"),
					parameterWithName("boardId").description("게시글 아이디")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

}
