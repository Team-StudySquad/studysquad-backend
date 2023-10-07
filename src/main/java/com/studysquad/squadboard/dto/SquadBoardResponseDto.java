package com.studysquad.squadboard.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SquadBoardResponseDto {

	private Long squadBoardId;
	private int missionSequence;
	private String squadBoardTitle;
	private String squadBoardContent;
	private String creator;

	@Builder
	@QueryProjection
	public SquadBoardResponseDto(Long squadBoardId, int missionSequence, String squadBoardTitle,
		String squadBoardContent,
		String creator) {
		this.squadBoardId = squadBoardId;
		this.missionSequence = missionSequence;
		this.squadBoardTitle = squadBoardTitle;
		this.squadBoardContent = squadBoardContent;
		this.creator = creator;
	}

}
