package com.studysquad.boardcomment.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardCommentEditDto {

	String boardCommentContent;

	@Builder
	public BoardCommentEditDto(String boardCommentContent) {
		this.boardCommentContent = boardCommentContent;
	}
}
