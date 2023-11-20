package com.studysquad.squad.dto;

import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SquadJoinDto {

	@NotNull(message = "역할을 선택해주세요")
	private boolean mentor;

	@Builder
	public SquadJoinDto(boolean mentor) {
		this.mentor = mentor;
	}
}
