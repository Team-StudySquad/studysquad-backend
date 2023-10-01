package com.studysquad.board.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.studysquad.board.response.BoardResponse;

public interface BoardRepositoryCustom {
	Optional<BoardResponse> getBoardById(Long boardId);

	//Page<BoardResponse> searchBoardPageByCondition()
}
