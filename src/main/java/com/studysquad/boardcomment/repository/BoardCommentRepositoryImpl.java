package com.studysquad.boardcomment.repository;

import static com.studysquad.board.domain.QBoard.*;
import static com.studysquad.boardcomment.domain.QBoardComment.*;
import static com.studysquad.user.domain.QUser.*;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.boardcomment.dto.BoardCommentResponse;
import com.studysquad.boardcomment.dto.QBoardCommentResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCommentRepositoryImpl implements BoardCommentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<BoardCommentResponse> getBoardComments(Long boardId) {
		return queryFactory.select(new QBoardCommentResponse(
				boardComment.boardCommentContent,
				user.nickname,
				boardComment.createAt))
			.from(boardComment)
			.join(user).on(user.id.eq(boardComment.user.id))
			.join(board).on(board.id.eq(boardComment.board.id))
			.where(boardComment.board.id.eq(boardId))
			.fetch();
	}
}
