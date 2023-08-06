package com.studysquad.mission.dto;

import javax.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionEditDto {

	@NotBlank(message = "타이틀을 입력하세요")
	private String missionTitle;
	@NotBlank(message = "컨텐츠를 입력하세요")
	private String missionContent;

	@Builder
	public MissionEditDto(String missionTitle, String missionContent) {
		this.missionTitle = missionTitle;
		this.missionContent = missionContent;
	}
}
