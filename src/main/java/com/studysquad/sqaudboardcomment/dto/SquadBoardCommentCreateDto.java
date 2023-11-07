package com.studysquad.sqaudboardcomment.dto;

import javax.validation.constraints.NotBlank;

import com.studysquad.sqaudboardcomment.domain.SquadBoardComment;
import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.user.domain.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SquadBoardCommentCreateDto {

	@NotBlank
	private String squadBoardCommentContent;

	public SquadBoardComment toEntity(SquadBoard squadBoard, User creator) {
		return SquadBoardComment.builder()
			.squadBoard(squadBoard)
			.user(creator)
			.squadBoardCommentContent(squadBoardCommentContent)
			.build();
	}

	@Builder
	public SquadBoardCommentCreateDto(String squadBoardCommentContent) {
		this.squadBoardCommentContent = squadBoardCommentContent;

	}
}
