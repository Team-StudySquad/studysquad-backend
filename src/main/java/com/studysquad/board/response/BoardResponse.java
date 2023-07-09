package com.studysquad.board.response;

import com.studysquad.board.domain.Board;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardResponse {
	private final Long Id;
	private final String title;
	private final String content; // 상수로 생성, Read Only

	public BoardResponse(Board board){
		this.Id = board.getId();
		this.title = board.getTitle();
		this.content = board.getContent();
	}

	@Builder
	public BoardResponse(Long id, String title, String content) {
		this.Id = id;
		this.title = title.substring(0, Math.min(title.length(), 10));
		this.content = content;
	}
}
