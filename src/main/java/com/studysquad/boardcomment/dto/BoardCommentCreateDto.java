package com.studysquad.boardcomment.dto;

import javax.validation.constraints.NotBlank;

import com.studysquad.board.domain.Board;
import com.studysquad.boardcomment.domain.BoardComment;
import com.studysquad.user.domain.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardCommentCreateDto {

	@NotBlank(message = "댓글을 작성 해주세요")
	private String boardCommentContent;

	@Builder
	public BoardCommentCreateDto(String boardCommentContent) {
		this.boardCommentContent = boardCommentContent;
	}

	public BoardComment toEntity(User user, Board board) {
		return BoardComment.builder()
			.user(user)
			.board(board)
			.boardCommentContent(boardCommentContent)
			.build();
	}
}
