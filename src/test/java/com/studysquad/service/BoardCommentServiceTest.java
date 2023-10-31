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

import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.boardcomment.domain.BoardComment;
import com.studysquad.boardcomment.dto.BoardCommentCreateDto;
import com.studysquad.boardcomment.dto.BoardCommentResponse;
import com.studysquad.boardcomment.repository.BoardCommentRepository;
import com.studysquad.boardcomment.service.BoardCommentService;
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

		List<BoardCommentResponse> boardCommentsResponse = IntStream.range(1, 31)
			.mapToObj(i -> BoardCommentResponse.builder()
				.boardCommentContent("boardCommentContent" + i)
				.creator("user" + i)
				.createAt(LocalDateTime.now())
				.build())
			.collect(Collectors.toList());

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
		User user = User.builder()
			.email("userA@aaa.com")
			.nickname("userA")
			.build();
		LoginUser loginUser = LoginUser.builder()
			.email(user.getEmail())
			.build();
		Board board = Board.builder()
			.user(user)
			.build();
		BoardCommentCreateDto request = BoardCommentCreateDto.builder()
			.boardCommentContent("boardCommentContent")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(boardRepository.findById(board.getId()))
			.thenReturn(Optional.of(board));

		boardCommentService.createBoardComment(board.getId(), loginUser, request);

		verify(boardCommentRepository, times(1)).save(any(BoardComment.class));
	}
}
