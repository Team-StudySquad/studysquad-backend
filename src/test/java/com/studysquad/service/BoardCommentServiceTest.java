package com.studysquad.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.boardcomment.domain.BoardComment;
import com.studysquad.boardcomment.dto.BoardCommentCreateDto;
import com.studysquad.boardcomment.dto.BoardCommentEditDto;
import com.studysquad.boardcomment.dto.BoardCommentResponse;
import com.studysquad.boardcomment.repository.BoardCommentRepository;
import com.studysquad.boardcomment.service.BoardCommentService;
import com.studysquad.global.error.exception.BoardInfoMismatchException;
import com.studysquad.global.error.exception.UserInfoMismatchException;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class BoardCommentServiceTest {

	@Mock
	UserRepository userRepository;
	@Mock
	BoardRepository boardRepository;
	@Mock
	BoardCommentRepository boardCommentRepository;
	@InjectMocks
	BoardCommentService boardCommentService;

	@Test
	@DisplayName("게시글 댓글 조회 성공")
	void successGetBoardComments() {
		Long boardId = 1L;

		List<BoardCommentResponse> boardCommentsResponse = createBoardCommentResponse();

		when(boardCommentRepository.getBoardComments(boardId))
			.thenReturn(boardCommentsResponse);

		List<BoardCommentResponse> result = boardCommentService.getBoardComments(boardId);

		assertThat(result.size()).isEqualTo(boardCommentsResponse.size());
		assertThat(result.get(0)).isEqualTo(boardCommentsResponse.get(0));
		assertThat(result.get(0).getCreator()).isEqualTo(boardCommentsResponse.get(0).getCreator());
	}

	@Test
	@DisplayName("게시글 댓글 생성 성공")
	void successCreateBoardComment() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);
		Board board = createBoard(user);
		BoardCommentCreateDto request = createBoardCommentCreateDto();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(boardRepository.findById(board.getId()))
			.thenReturn(Optional.of(board));

		boardCommentService.createBoardComment(board.getId(), loginUser, request);

		verify(boardCommentRepository, times(1)).save(any(BoardComment.class));
	}

	@Test
	@DisplayName("게시글 댓글 수정 성공")
	void successEditBoardComment() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);
		Board board = createBoard(user);
		BoardComment boardComment = createBoardComment(user, board);
		BoardCommentEditDto editRequest = createBoardCommentEditDto();

		ReflectionTestUtils.setField(user, "id", 1L);
		ReflectionTestUtils.setField(board, "id", 1L);
		ReflectionTestUtils.setField(boardComment, "id", 1L);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(boardRepository.findById(board.getId()))
			.thenReturn(Optional.of(board));
		when(boardCommentRepository.findByIdWithUserAndBoard(boardComment.getId()))
			.thenReturn(Optional.of(boardComment));

		boardCommentService.editBoardComment(board.getId(), boardComment.getId(), editRequest, loginUser);

		assertThat(boardComment.getBoardCommentContent()).isEqualTo(editRequest.getBoardCommentContent());
	}

	@Test
	@DisplayName("게시글 정보가 일치 하지 않은 경우 게시글 댓글 수정 실패")
	void failEditBoardCommentByMismatchBoardInfo() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);
		Board board = createBoard(user);
		Board mismatchBoard = createBoard(user);
		BoardComment boardComment = createBoardComment(user, board);
		BoardCommentEditDto editRequest = createBoardCommentEditDto();

		ReflectionTestUtils.setField(user, "id", 1L);
		ReflectionTestUtils.setField(board, "id", 1L);
		ReflectionTestUtils.setField(mismatchBoard, "id", 2L);
		ReflectionTestUtils.setField(boardComment, "id", 1L);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(boardRepository.findById(mismatchBoard.getId()))
			.thenReturn(Optional.of(mismatchBoard));
		when(boardCommentRepository.findByIdWithUserAndBoard(boardComment.getId()))
			.thenReturn(Optional.of(boardComment));

		assertThatThrownBy(
			() -> boardCommentService.editBoardComment(mismatchBoard.getId(), boardComment.getId(), editRequest,
				loginUser))
			.isInstanceOf(BoardInfoMismatchException.class)
			.message().isEqualTo("게시글 정보가 일치하지 않습니다");
	}

	@Test
	@DisplayName("사용자 정보가 일치하지 않는 경우 게시글 댓글 수정 실패")
	void failEditBoardCommentByMismatchUserInfo() {
		User user = createUser();
		User mismatchUser = createMismatchUser();
		LoginUser loginUser = createLoginUser(mismatchUser);
		Board board = createBoard(user);
		BoardComment boardComment = createBoardComment(user, board);
		BoardCommentEditDto editRequest = createBoardCommentEditDto();

		ReflectionTestUtils.setField(user, "id", 1L);
		ReflectionTestUtils.setField(mismatchUser, "id", 2L);
		ReflectionTestUtils.setField(board, "id", 1L);
		ReflectionTestUtils.setField(boardComment, "id", 1L);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(mismatchUser));
		when(boardRepository.findById(board.getId()))
			.thenReturn(Optional.of(board));
		when(boardCommentRepository.findByIdWithUserAndBoard(boardComment.getId()))
			.thenReturn(Optional.of(boardComment));

		assertThatThrownBy(
			() -> boardCommentService.editBoardComment(board.getId(), boardComment.getId(), editRequest,
				loginUser))
			.isInstanceOf(UserInfoMismatchException.class)
			.message().isEqualTo("사용자 정보가 일치하지 않습니다");

	}

	@Test
	@DisplayName("게시글 댓글 삭제 성공")
	void successDeleteBoardComment() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);
		Board board = createBoard(user);
		BoardComment boardComment = createBoardComment(user, board);

		ReflectionTestUtils.setField(user, "id", 1L);
		ReflectionTestUtils.setField(board, "id", 1L);
		ReflectionTestUtils.setField(boardComment, "id", 1L);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(boardRepository.findById(board.getId()))
			.thenReturn(Optional.of(board));
		when(boardCommentRepository.findByIdWithUserAndBoard(boardComment.getId()))
			.thenReturn(Optional.of(boardComment));

		boardCommentService.deleteBoardComment(board.getId(), boardComment.getId(), loginUser);

		verify(boardCommentRepository, times(1)).deleteById(boardComment.getId());
	}

	@Test
	@DisplayName("게시글 정보가 일치하지 않는 경우 게시글 댓글 삭제 실패")
	void failDeleteBoardCommentByMismatchBoardInfo() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);
		Board board = createBoard(user);
		Board mismatchBoard = createBoard(user);
		BoardComment boardComment = createBoardComment(user, board);

		ReflectionTestUtils.setField(user, "id", 1L);
		ReflectionTestUtils.setField(board, "id", 1L);
		ReflectionTestUtils.setField(mismatchBoard, "id", 2L);
		ReflectionTestUtils.setField(boardComment, "id", 1L);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(boardRepository.findById(mismatchBoard.getId()))
			.thenReturn(Optional.of(mismatchBoard));
		when(boardCommentRepository.findByIdWithUserAndBoard(boardComment.getId()))
			.thenReturn(Optional.of(boardComment));

		assertThatThrownBy(
			() -> boardCommentService.deleteBoardComment(mismatchBoard.getId(), boardComment.getId(), loginUser))
			.isInstanceOf(BoardInfoMismatchException.class)
			.message().isEqualTo("게시글 정보가 일치하지 않습니다");
	}

	@Test
	@DisplayName("사용자 정보가 일치하지 않는 경우 게시글 댓글 삭제 실패")
	void failDeleteBoardCommentByMismatchUserInfo() {
		User user = createUser();
		User mismatchUser = createMismatchUser();
		LoginUser loginUser = createLoginUser(mismatchUser);
		Board board = createBoard(user);
		BoardComment boardComment = createBoardComment(user, board);

		ReflectionTestUtils.setField(user, "id", 1L);
		ReflectionTestUtils.setField(mismatchUser, "id", 2L);
		ReflectionTestUtils.setField(board, "id", 1L);
		ReflectionTestUtils.setField(boardComment, "id", 1L);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(mismatchUser));
		when(boardRepository.findById(board.getId()))
			.thenReturn(Optional.of(board));
		when(boardCommentRepository.findByIdWithUserAndBoard(boardComment.getId()))
			.thenReturn(Optional.of(boardComment));

		assertThatThrownBy(() -> boardCommentService.deleteBoardComment(board.getId(), boardComment.getId(), loginUser))
			.isInstanceOf(UserInfoMismatchException.class)
			.message().isEqualTo("사용자 정보가 일치하지 않습니다");
	}

	private User createUser() {
		return User.builder()
			.email("aaa@aaa.com")
			.nickname("userA")
			.build();
	}

	private User createMismatchUser() {
		return User.builder()
			.email("mistmatch@mistmatch.com")
			.nickname("mismatchUser")
			.build();
	}

	private List<BoardCommentResponse> createBoardCommentResponse() {
		return IntStream.range(1, 31)
			.mapToObj(i -> BoardCommentResponse.builder()
				.boardCommentContent("boardCommentContent" + i)
				.creator("user" + i)
				.createAt(LocalDateTime.now())
				.build())
			.collect(Collectors.toList());
	}

	private LoginUser createLoginUser(User user) {
		return LoginUser.builder()
			.email(user.getEmail())
			.build();
	}

	private Board createBoard(User user) {
		return Board.builder()
			.user(user)
			.build();
	}

	private BoardComment createBoardComment(User user, Board board) {
		return BoardComment.builder()
			.user(user)
			.board(board)
			.boardCommentContent("boardCommentContent")
			.build();
	}

	private BoardCommentCreateDto createBoardCommentCreateDto() {
		return BoardCommentCreateDto.builder()
			.boardCommentContent("boardCommentContent")
			.build();
	}

	private BoardCommentEditDto createBoardCommentEditDto() {
		return BoardCommentEditDto.builder()
			.boardCommentContent("Edit boardCommentContent")
			.build();
	}
}