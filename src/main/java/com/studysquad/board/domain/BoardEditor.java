package com.studysquad.board.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardEditor {

	private final String title;
	private final String content;

	@Builder
	public BoardEditor(String title, String content) {
		this.title = title;
		this.content = content;
	}
}
