package com.studysquad.squadboard.dto;

import javax.validation.constraints.NotBlank;

import com.studysquad.mission.domain.Mission;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.user.domain.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SquadBoardCreateDto {

	@NotBlank
	private String squadBoardContent;
	@NotBlank
	private String squadBoardTitle;

	public SquadBoard toEntity(Squad squad, User user, Mission mission) {
		return SquadBoard.builder()
			.user(user)
			.squad(squad)
			.mission(mission)
			.squadBoardTitle(squadBoardTitle)
			.squadBoardContent(squadBoardContent)
			.build();
	}

	@Builder
	public SquadBoardCreateDto(String squadBoardContent, String squadBoardTitle) {
		this.squadBoardContent = squadBoardContent;
		this.squadBoardTitle = squadBoardTitle;
	}

}
