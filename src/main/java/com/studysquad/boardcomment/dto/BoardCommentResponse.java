package com.studysquad.boardcomment.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardCommentResponse {

	private Long boardCommentId;
	private String boardCommentContent;
	private String creator;
	private LocalDateTime createAt;

	@Builder
	@QueryProjection
	public BoardCommentResponse(Long boardCommentId, String boardCommentContent, String creator,
		LocalDateTime createAt) {
		this.boardCommentId = boardCommentId;
		this.boardCommentContent = boardCommentContent;
		this.creator = creator;
		this.createAt = createAt;
	}
}
