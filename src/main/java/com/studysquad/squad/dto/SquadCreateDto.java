package com.studysquad.squad.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SquadCreateDto {

	@NotBlank(message = "카테고리를 설정해주세요")
	private String categoryName;
	@NotBlank(message = "스쿼드 이름을 작성해주세요")
	private String squadName;
	@NotBlank(message = "스쿼드 설명을 작성해주세요")
	private String squadExplain;
	@NotNull(message = "역할을 입력해주세요")
	private boolean isMentor;

	@Builder
	public SquadCreateDto(String categoryName, String squadName, String squadExplain, boolean isMentor) {
		this.categoryName = categoryName;
		this.squadName = squadName;
		this.squadExplain = squadExplain;
		this.isMentor = isMentor;
	}
}
