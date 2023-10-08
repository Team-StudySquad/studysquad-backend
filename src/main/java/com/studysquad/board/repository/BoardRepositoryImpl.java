package com.studysquad.board.repository;

import static com.studysquad.board.domain.QBoard.*;
import static com.studysquad.category.domain.QCategory.*;
import static com.studysquad.mission.domain.QMission.*;
import static com.studysquad.squad.domain.QSquad.*;
import static com.studysquad.user.domain.QUser.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.board.request.BoardSearchCondition;
import com.studysquad.board.response.BoardResponse;
import com.studysquad.board.response.QBoardResponse;

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
				category.categoryName,
				squad.squadName,
				mission.missionTitle,
				mission.missionContent,
				board.title,
				board.content))
			.from(board)
			.join(user).on(board.user.id.eq(user.id))
			.join(squad).on(board.squad.id.eq(squad.id))
			.join(mission).on(board.mission.id.eq(mission.id))
			.join(category).on(squad.category.id.eq(category.id))
			.where(board.id.eq(boardId))
			.fetchOne();

		return Optional.ofNullable(fetchOne);
	}

	@Override
	public Page<BoardResponse> getBoards(BoardSearchCondition searchCondition, Pageable pageable) {
		List<BoardResponse> fetch = queryFactory.select(new QBoardResponse(
				board.id,
				user.nickname,
				category.categoryName,
				squad.squadName,
				mission.missionTitle,
				mission.missionContent,
				board.title,
				board.content
			))
			.from(board)
			.join(user).on(board.user.id.eq(user.id))
			.join(squad).on(board.squad.id.eq(squad.id))
			.join(mission).on(board.mission.id.eq(mission.id))
			.join(category).on(squad.category.id.eq(category.id))
			.where(categoryNameEq(searchCondition.getCategoryName()))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory.select(board.count())
			.from(board)
			.join(user).on(board.user.id.eq(user.id))
			.join(squad).on(board.squad.id.eq(squad.id))
			.join(mission).on(board.mission.id.eq(mission.id))
			.join(category).on(squad.category.id.eq(category.id))
			.where(categoryNameEq(searchCondition.getCategoryName()));

		return PageableExecutionUtils.getPage(fetch, pageable, countQuery::fetchOne);
	}

	private BooleanExpression categoryNameEq(String categoryName) {
		return !StringUtils.hasText(categoryName) ? null : category.categoryName.eq(categoryName);
	}
}
