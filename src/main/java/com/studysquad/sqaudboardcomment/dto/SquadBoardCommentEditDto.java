package com.studysquad.sqaudboardcomment.dto;

import javax.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SquadBoardCommentEditDto {

	@NotBlank
	private String squadBoardCommentContent;

	@Builder
	public SquadBoardCommentEditDto(String commentContent) {
		this.squadBoardCommentContent = commentContent;
	}
}
