package com.studysquad.squad.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.studysquad.squad.domain.SquadStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSquadResponseDto {
	private Long squadId;
	private String squadName;
	private String squadExplain;
	private String categoryName;
	private SquadStatus squadStatus;

	@Builder
	@QueryProjection
	public UserSquadResponseDto(Long squadId, String squadName, String squadExplain, String categoryName,
		SquadStatus squadStatus) {
		this.squadId = squadId;
		this.squadName = squadName;
		this.squadExplain = squadExplain;
		this.categoryName = categoryName;
		this.squadStatus = squadStatus;
	}
}
