package com.studysquad.sqaudboardcomment.repository;

import static com.studysquad.sqaudboardcomment.domain.QSquadBoardComment.*;
import static com.studysquad.squadboard.domain.QSquadBoard.*;
import static com.studysquad.user.domain.QUser.*;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.sqaudboardcomment.dto.QSquadBoardCommentResponseDto;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentResponseDto;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
@Transactional(readOnly = true)
public class SquadBoardCommentRepositoryImpl implements SquadBoardCommentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Boolean isUserOfSquadBoardComment(Long userId, Long squadBoardId, Long commentId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(squadBoardComment)
			.where(
				squadBoardComment.id.eq(commentId)
					.and(squadBoardComment.user.id.eq(userId))
					.and(squadBoardComment.squadBoard.id.eq(squadBoardId)))
			.fetchOne();
		return fetchOne != null;
	}

	@Override
	public List<SquadBoardCommentResponseDto> getSquadBoardComments(Long squadBoardId) {
		return queryFactory.select(new QSquadBoardCommentResponseDto(
				squadBoardComment.id,
				squadBoardComment.squadBoardCommentContent,
				user.nickname,
				squadBoardComment.createAt
			))
			.from(squadBoardComment)
			.join(user).on(squadBoardComment.user.id.eq(user.id))
			.join(squadBoard).on(squadBoardComment.squadBoard.id.eq(squadBoard.id))
			.where(squadBoardComment.squadBoard.id.eq(squadBoardId))
			.fetch();
	}
}
