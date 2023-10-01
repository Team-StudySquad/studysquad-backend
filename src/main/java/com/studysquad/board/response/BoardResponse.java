package com.studysquad.board.response;

import com.querydsl.core.annotations.QueryProjection;
import com.studysquad.board.domain.Board;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardResponse {
	private Long boardId;
	private String nickname;
	private String title;
	private String content;
	// todo. 스쿼드 이름 추가 할지 결정하고 추가할 것

	@Builder
	@QueryProjection
	public BoardResponse(Long boardId, String nickname, String title, String content) {
		this.boardId = boardId;
		this.nickname = nickname;
		this.title = title;
		this.content = content;
	}
}
