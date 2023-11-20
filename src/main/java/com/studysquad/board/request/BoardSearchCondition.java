package com.studysquad.board.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardSearchCondition {
	private String categoryName;

	@Builder
	public BoardSearchCondition(String categoryName) {
		this.categoryName = categoryName;
	}
}
