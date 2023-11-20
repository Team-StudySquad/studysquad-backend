package com.studysquad.squad.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SquadSearchCondition {
	private Boolean mentor;
	private String categoryName;

	@Builder
	public SquadSearchCondition(Boolean mentor, String categoryName) {
		this.mentor = mentor;
		this.categoryName = categoryName;
	}
}
