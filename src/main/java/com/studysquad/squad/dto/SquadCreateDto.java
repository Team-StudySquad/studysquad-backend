package com.studysquad.squad.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SquadCreateDto {

	@NotBlank(message = "카테고리를 설정해주세요")
	private String categoryName;
	@NotBlank(message = "스쿼드 이름을 작성해주세요")
	private String squadName;
	@NotBlank(message = "스쿼드 설명을 작성해주세요")
	private String squadExplain;
	@NotNull(message = "역할을 입력해주세요")
	private boolean mentor;

	@Builder
	public SquadCreateDto(String categoryName, String squadName, String squadExplain, boolean mentor) {
		this.categoryName = categoryName;
		this.squadName = squadName;
		this.squadExplain = squadExplain;
		this.mentor = mentor;
	}
}
