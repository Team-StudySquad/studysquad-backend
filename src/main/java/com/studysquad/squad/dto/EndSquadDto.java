package com.studysquad.squad.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EndSquadDto {

	private Long squadId;
	private String squadName;
	private String squadExplain;
	private String categoryName;

	@Builder
	@QueryProjection
	public EndSquadDto(Long squadId, String squadName, String squadExplain, String categoryName) {
		this.squadId = squadId;
		this.squadName = squadName;
		this.squadExplain = squadExplain;
		this.categoryName = categoryName;
	}
}
