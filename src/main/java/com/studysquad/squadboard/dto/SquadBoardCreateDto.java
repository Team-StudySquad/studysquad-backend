package com.studysquad.squadboard.dto;

import javax.validation.constraints.NotBlank;

import com.studysquad.squadboard.domain.SquadBoard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SquadBoardCreateDto {

	@NotBlank
	private String squadBoardContent;
	@NotBlank
	private String squadBoardTitle;

	public SquadBoard toEntity() {
		return SquadBoard.builder()
			.squadBoardTitle(squadBoardTitle)
			.squadBoardContent(squadBoardContent)
			.build();
	}

	@Builder
	public SquadBoardCreateDto(String squadBoardContent, String squadBoardTitle) {
		this.squadBoardContent = squadBoardContent;
		this.squadBoardTitle = squadBoardTitle;
	}

}
