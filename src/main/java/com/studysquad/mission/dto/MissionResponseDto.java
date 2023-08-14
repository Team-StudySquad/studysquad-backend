package com.studysquad.mission.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.studysquad.mission.domain.MissionStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionResponseDto {
	private Long missionId;
	private String missionTitle;
	private String missionContent;
	private Integer missionSequence;
	private MissionStatus missionStatus;

	@QueryProjection
	@Builder
	public MissionResponseDto(Long missionId, String missionTitle, String missionContent, Integer missionSequence,
		MissionStatus missionStatus) {
		this.missionId = missionId;
		this.missionTitle = missionTitle;
		this.missionContent = missionContent;
		this.missionSequence = missionSequence;
		this.missionStatus = missionStatus;
	}
}
