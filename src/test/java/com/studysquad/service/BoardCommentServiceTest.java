package com.studysquad.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studysquad.boardcomment.dto.BoardCommentResponse;
import com.studysquad.boardcomment.repository.BoardCommentRepository;
import com.studysquad.boardcomment.service.BoardCommentService;

@ExtendWith(MockitoExtension.class)
public class BoardCommentServiceTest {

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
}
