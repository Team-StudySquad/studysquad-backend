package com.studysquad.mission.dto;

import javax.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionCreateDto {

	@NotBlank(message = "미션 이름을 작성 해주세요")
	private String missionTitle;
	@NotBlank(message = "미션 내용을 작성 해주세요")
	private String missionContent;
	@NotBlank(message = "미션 순서가 설정되지 않았습니다")
	private int missionSequence;

	@Builder
	public MissionCreateDto(String missionTitle, String missionContent, int missionSequence) {
		this.missionTitle = missionTitle;
		this.missionContent = missionContent;
		this.missionSequence = missionSequence;
	}
}
