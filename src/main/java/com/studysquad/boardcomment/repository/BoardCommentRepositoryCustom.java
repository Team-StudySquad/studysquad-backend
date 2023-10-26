package com.studysquad.boardcomment.repository;

import java.util.List;

import com.studysquad.boardcomment.dto.BoardCommentResponse;

public interface BoardCommentRepositoryCustom {

	List<BoardCommentResponse> getBoardComments(Long boardId);
}
