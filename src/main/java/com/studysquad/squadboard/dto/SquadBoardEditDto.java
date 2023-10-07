package com.studysquad.squadboard.dto;

import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SquadBoardEditDto {

	@NotBlank
	private String squadBoardTitle;
	@NotBlank
	private String squadBoardContent;

	@Builder
	public SquadBoardEditDto(String squadBoardTitle, String squadBoardContent) {
		this.squadBoardTitle = squadBoardTitle;
		this.squadBoardContent = squadBoardContent;
	}
}
