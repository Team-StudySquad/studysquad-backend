package com.studysquad.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.boardcomment.domain.BoardComment;
import com.studysquad.boardcomment.dto.BoardCommentCreateDto;
import com.studysquad.boardcomment.dto.BoardCommentEditDto;
import com.studysquad.boardcomment.repository.BoardCommentRepository;
import com.studysquad.user.domain.User;
import com.studysquad.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class BoardCommentControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	UserRepository userRepository;
	@Autowired
	BoardRepository boardRepository;
	@Autowired
	BoardCommentRepository boardCommentRepository;
	@Autowired
	ObjectMapper objectMapper;

	@BeforeEach
	void init() {
		boardCommentRepository.deleteAll();
		boardRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("게시글 댓글 전체 조회 성공")
	void successGetBoardComments() throws Exception {
		User user = userRepository.save(User.builder()
			.email("aaa@aaa.com")
			.nickname("userA")
			.build());

		Board board = boardRepository.save(Board.builder()
			.user(user)
			.build());

		List<BoardComment> boardComments = LongStream.range(1, 31)
			.mapToObj(i -> BoardComment.builder()
				.user(user)
				.board(board)
				.boardCommentContent("content" + i)
				.build())
			.collect(Collectors.toList());

		boardCommentRepository.saveAll(boardComments);

		mockMvc.perform(get("/api/board/{boardId}/boardcomments", board.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("게시글 댓글 조회 성공"))
			.andExpect(jsonPath("$.data.length()").value(boardComments.size()))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 생성 성공")
	void successCreateBoardComment() throws Exception {
		User user = userRepository.save(User.builder()
			.email("aaa@aaa.com")
			.nickname("userA")
			.build());

		Board board = boardRepository.save(Board.builder()
			.user(user)
			.build());

		BoardCommentCreateDto request = BoardCommentCreateDto.builder()
			.boardCommentContent("boardCommentContent")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/board/{boardId}/boardcomment", board.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
			.andExpect(jsonPath("$.message").value("게시글 댓글 생성 성공"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("댓글을 작성 하지 않고 생성 요청 시 댓글 생성 실패")
	void failCreateBoardCommentWithEmptyData() throws Exception {
		User user = userRepository.save(User.builder()
			.email("aaa@aaa.com")
			.nickname("userA")
			.build());

		Board board = boardRepository.save(Board.builder()
			.user(user)
			.build());

		BoardCommentCreateDto request = BoardCommentCreateDto.builder()
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/board/{boardId}/boardcomment", board.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다"))
			.andExpect(jsonPath("$.validation.boardCommentContent").value("댓글을 작성 해주세요"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 댓글 수정 성공")
	void successEditBoardComment() throws Exception {
		User user = userRepository.save(User.builder()
			.email("aaa@aaa.com")
			.nickname("userA")
			.build());
		Board board = boardRepository.save(Board.builder()
			.user(user)
			.build());
		BoardComment boardComment = boardCommentRepository.save(BoardComment.builder()
			.user(user)
			.board(board)
			.boardCommentContent("boardCommentContent")
			.build());
		BoardCommentEditDto request = BoardCommentEditDto.builder()
			.boardCommentContent("edit BoardCommentContent")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(
				patch("/api/board/{boardId}/boardcomment/{boardCommentId}", board.getId(), boardComment.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("게시글 댓글 수정 성공"))
			.andDo(print());

		BoardComment result = boardCommentRepository.findById(boardComment.getId()).get();

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(boardComment.getId());
		assertThat(result.getBoardCommentContent()).isEqualTo(request.getBoardCommentContent());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 정보가 일치하지 않으면 게시글 댓글 수정 실패")
	void failEditBoardCommentWithMismatchBoardInfo() throws Exception {
		User user = userRepository.save(User.builder()
			.email("aaa@aaa.com")
			.nickname("userA")
			.build());
		Board board = boardRepository.save(Board.builder()
			.user(user)
			.build());
		Board mismatchBoard = boardRepository.save(Board.builder()
			.user(user)
			.build());
		BoardComment boardComment = boardCommentRepository.save(BoardComment.builder()
			.user(user)
			.board(board)
			.build());
		BoardCommentEditDto request = BoardCommentEditDto.builder()
			.boardCommentContent("edit boardCommentContent")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(
				patch("/api/board/{boardId}/boardcomment/{boardCommentId}", mismatchBoard.getId(), boardComment.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("게시글 정보가 일치하지 않습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("사용자 정보가 일치하지 않으면 게시글 댓글 수정 실패")
	void failEditBoardCommentWithMismatchUserInfo() throws Exception {
		User user = userRepository.save(User.builder()
			.email("user@aaa.com")
			.nickname("userA")
			.build());
		User requestUser = userRepository.save(User.builder()
			.email("aaa@aaa.com")
			.nickname("requestUser")
			.build());
		Board board = boardRepository.save(Board.builder()
			.user(user)
			.build());
		BoardComment boardComment = boardCommentRepository.save(BoardComment.builder()
			.user(user)
			.board(board)
			.build());
		BoardCommentEditDto request = BoardCommentEditDto.builder()
			.boardCommentContent("edit boardCommentContent")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(
				patch("/api/board/{boardId}/boardcomment/{boardCommentId}", board.getId(), boardComment.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("사용자 정보가 일치하지 않습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 댓글 삭제 성공")
	void successDeleteBoardComment() throws Exception {
		User user = userRepository.save(User.builder()
			.email("aaa@aaa.com")
			.nickname("userA")
			.build());
		Board board = boardRepository.save(Board.builder()
			.user(user)
			.build());
		BoardComment boardComment = boardCommentRepository.save(BoardComment.builder()
			.user(user)
			.board(board)
			.boardCommentContent("boardCommentContent")
			.build());

		mockMvc.perform(
				delete("/api/board/{boardId}/boardcomment/{boardCommentId}", board.getId(), boardComment.getId())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("게시글 삭제 성공"))
			.andDo(print());

		Optional<BoardComment> result = boardCommentRepository.findById(boardComment.getId());

		assertThat(result).isEmpty();
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 정보가 일치하지 않으면 게시글 댓글 삭제 실패")
	void failDeleteBoardCommentWithMismatchBoardInfo() throws Exception {
		User user = userRepository.save(User.builder()
			.email("aaa@aaa.com")
			.nickname("userA")
			.build());
		Board board = boardRepository.save(Board.builder()
			.user(user)
			.build());
		Board mismatchBoard = boardRepository.save(Board.builder()
			.user(user)
			.build());
		BoardComment boardComment = boardCommentRepository.save(BoardComment.builder()
			.user(user)
			.board(board)
			.boardCommentContent("boardCommentContent")
			.build());

		mockMvc.perform(
				delete("/api/board/{baordId}/boardcomment/{boardCommentId}", mismatchBoard.getId(), boardComment.getId())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("게시글 정보가 일치하지 않습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("사용자 정보가 일치하지 않으면 게시글 댓글 삭제 실패")
	void failDeleteBoardCommentWithMismatchUserInfo() throws Exception {
		User user = userRepository.save(User.builder()
			.email("userA@aaa.com")
			.nickname("userA")
			.build());
		User mismatchUser = userRepository.save(User.builder()
			.email("aaa@aaa.com")
			.nickname("mismatchUser")
			.build());
		Board board = boardRepository.save(Board.builder()
			.user(user)
			.build());
		BoardComment boardComment = boardCommentRepository.save(BoardComment.builder()
			.user(user)
			.board(board)
			.boardCommentContent("boardCommentContent")
			.build());

		mockMvc.perform(
				delete("/api/board/{baordId}/boardcomment/{boardCommentId}", board.getId(), boardComment.getId())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("사용자 정보가 일치하지 않습니다"))
			.andDo(print());
	}
}
