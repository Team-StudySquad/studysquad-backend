package com.studysquad.squad.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SquadCreateDto {

	private String categoryName;
	private String squadName;
	private String squadExplain;
	private boolean isMentor;

	@Builder
	public SquadCreateDto(String categoryName, String squadName, String squadExplain, boolean isMentor) {
		this.categoryName = categoryName;
		this.squadName = squadName;
		this.squadExplain = squadExplain;
		this.isMentor = isMentor;
	}
}
