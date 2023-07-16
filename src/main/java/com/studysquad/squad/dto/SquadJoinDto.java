package com.studysquad.squad.dto;

import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SquadJoinDto {

	@NotBlank(message = "역할을 선택해주세요")
	boolean isMentor;

	@Builder
	public SquadJoinDto(boolean isMentor) {
		this.isMentor = isMentor;
	}
}
