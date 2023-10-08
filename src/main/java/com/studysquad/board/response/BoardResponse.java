package com.studysquad.board.response;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardResponse {
	private Long boardId;
	private String creator;
	private String categoryName;
	private String squadName;
	private String missionTitle;
	private String missionContent;
	private String boardTitle;
	private String boardContent;

	@Builder
	@QueryProjection
	public BoardResponse(Long boardId, String creator, String categoryName, String squadName, String missionTitle,
		String missionContent,
		String boardTitle, String boardContent) {
		this.boardId = boardId;
		this.creator = creator;
		this.categoryName = categoryName;
		this.squadName = squadName;
		this.missionTitle = missionTitle;
		this.missionContent = missionContent;
		this.boardTitle = boardTitle;
		this.boardContent = boardContent;
	}
}
