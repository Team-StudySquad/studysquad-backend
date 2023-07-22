package com.studysquad.squad.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SquadResponseDto {

	private Long squadId;
	private Long userCount;
	private String squadName;
	private String squadExplain;
	private String categoryName;

	@QueryProjection
	@Builder
	public SquadResponseDto(Long squadId, Long userCount, String squadName, String squadExplain, String categoryName) {
		this.squadId = squadId;
		this.userCount = userCount;
		this.squadName = squadName;
		this.squadExplain = squadExplain;
		this.categoryName = categoryName;
	}

}
