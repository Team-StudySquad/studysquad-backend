package com.studysquad.squad.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessSquadDto {
	private Long squadId;
	private String categoryName;
	private String squadName;
	private String squadExplain;

	@Builder
	@QueryProjection
	public ProcessSquadDto(Long squadId, String categoryName, String squadName, String squadExplain) {
		this.squadId = squadId;
		this.categoryName = categoryName;
		this.squadName = squadName;
		this.squadExplain = squadExplain;
	}
}
