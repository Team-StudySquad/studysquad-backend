package com.studysquad.board.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.studysquad.board.request.BoardSearchCondition;
import com.studysquad.board.response.BoardResponse;

public interface BoardRepositoryCustom {
	Optional<BoardResponse> getBoardById(Long boardId);

	Page<BoardResponse> getBoards(BoardSearchCondition searchCondition, Pageable pageable);
}
