package com.studysquad.board.repository;

import static com.studysquad.board.domain.QBoard.*;
import static com.studysquad.user.domain.QUser.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.board.domain.QBoard;
import com.studysquad.board.response.BoardResponse;
import com.studysquad.board.response.QBoardResponse;
import com.studysquad.user.domain.QUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardRepositoryImpl implements BoardRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<BoardResponse> getBoardById(Long boardId) {
		BoardResponse fetchOne = queryFactory.select(new QBoardResponse(
				board.id,
				user.nickname,
				board.title,
				board.content))
			.from(board)
			.join(user)
			.on(board.user.id.eq(user.id))
			.where(board.id.eq(boardId))
			.fetchOne();

		return Optional.ofNullable(fetchOne);
	}
}
