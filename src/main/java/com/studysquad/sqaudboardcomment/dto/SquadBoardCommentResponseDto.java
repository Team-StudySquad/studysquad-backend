package com.studysquad.sqaudboardcomment.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SquadBoardCommentResponseDto {

	private Long squadBoardCommentId;
	private String squadBoardCommentContent;
	private String creator;
	private LocalDateTime createAt;

	@Builder
	@QueryProjection
	public SquadBoardCommentResponseDto(Long squadBoardCommentId, String commentContent, String creator,
		LocalDateTime createAt) {
		this.squadBoardCommentId = squadBoardCommentId;
		this.squadBoardCommentContent = commentContent;
		this.creator = creator;
		this.createAt = createAt;
	}
}
