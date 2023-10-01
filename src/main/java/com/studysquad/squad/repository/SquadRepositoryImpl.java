package com.studysquad.squad.repository;

import static com.studysquad.category.domain.QCategory.*;
import static com.studysquad.squad.domain.QSquad.*;
import static com.studysquad.user.domain.QUser.*;
import static com.studysquad.usersquad.domain.QUserSquad.*;

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
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.dto.EndSquadDto;
import com.studysquad.squad.dto.ProcessSquadDto;
import com.studysquad.squad.dto.QEndSquadDto;
import com.studysquad.squad.dto.QProcessSquadDto;
import com.studysquad.squad.dto.QSquadResponseDto;
import com.studysquad.squad.dto.QUserSquadResponseDto;
import com.studysquad.squad.dto.SquadResponseDto;
import com.studysquad.squad.dto.SquadSearchCondition;
import com.studysquad.squad.dto.UserSquadResponseDto;
import com.studysquad.usersquad.domain.QUserSquad;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SquadRepositoryImpl implements SquadRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Boolean isUserOfSquad(Long squadId, Long userId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(squad)
			.join(userSquad).on(userSquad.squad.id.eq(squad.id))
			.where(squad.id.eq(squadId)
				.and(userSquad.user.id.eq(userId)))
			.fetchFirst();

		return fetchOne != null;
	}

	@Override
	public Boolean isMentorOfSquad(Long squadId, Long userId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(squad)
			.join(userSquad).on(userSquad.squad.id.eq(squad.id))
			.where(squad.id.eq(squadId)
				.and(userSquad.user.id.eq(userId))
				.and(userSquad.isMentor.isTrue()))
			.fetchFirst();

		return fetchOne != null;
	}

	@Override
	public Boolean isSquadActive(Long squadId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(squad)
			.where(squad.id.eq(squadId)
				.and(squad.squadStatus.eq(SquadStatus.PROCESS)))
			.fetchOne();
		return fetchOne != null;
	}

	@Override
	public Optional<ProcessSquadDto> getProcessSquad(Long userId) {
		ProcessSquadDto fetchOne = queryFactory.select(new QProcessSquadDto(
				squad.id,
				category.categoryName,
				squad.squadName,
				squad.squadExplain))
			.from(squad)
			.join(squad.category, category)
			.join(userSquad).on(squad.id.eq(userSquad.squad.id))
			.where(squad.squadStatus.eq(SquadStatus.PROCESS)
				.and(userSquad.user.id.eq(userId)))
			.fetchOne();

		return Optional.ofNullable(fetchOne);
	}

	@Override
	public Optional<SquadResponseDto> findSquadBySquadId(Long squadId) {
		SquadResponseDto fetchOne = queryFactory
			.select(new QSquadResponseDto(
				squad.id,
				userSquad.user.count().as("userCount"),
				squad.squadName,
				squad.squadExplain,
				category.categoryName,
				user.nickname.max().as("creatorName")))
			.from(squad)
			.join(userSquad).on(squad.id.eq(userSquad.squad.id))
			.join(category).on(squad.category.id.eq(category.id))
			.leftJoin(user).on(userSquad.user.id.eq(user.id).and(userSquad.isCreator.isTrue()))
			.where(squad.id.eq(squadId).and(squad.squadStatus.eq(SquadStatus.RECRUIT)))
			.groupBy(squad.id)
			.fetchOne();
		return Optional.ofNullable(fetchOne);
	}

	@Override
	public Optional<EndSquadDto> getEndSquad(Long squadId, Long userId) {
		EndSquadDto fetchOne = queryFactory
			.select(new QEndSquadDto(
				squad.id,
				squad.squadName,
				squad.squadExplain,
				category.categoryName))
			.from(squad)
			.join(category).on(squad.category.id.eq(category.id))
			.join(userSquad).on(squad.id.eq(userSquad.squad.id))
			.where(squad.id.eq(squadId).and(userSquad.user.id.eq(userId))
				.and(squad.squadStatus.eq(SquadStatus.END)))
			.fetchOne();
		return Optional.ofNullable(fetchOne);
	}

	@Override
	public Page<SquadResponseDto> searchSquadPageByCondition(SquadSearchCondition searchCondition, Pageable pageable) {
		QUserSquad userSquad = new QUserSquad("userSquad");
		QUserSquad mentorUserSquad = new QUserSquad("mentorUserSquad");

		List<SquadResponseDto> fetch = queryFactory
			.select(new QSquadResponseDto(
				squad.id,
				userSquad.user.count().as("userCount"),
				squad.squadName,
				squad.squadExplain,
				category.categoryName,
				user.nickname.max().as("creatorName")
			))
			.from(squad)
			.join(userSquad)
			.on(squad.id.eq(userSquad.squad.id))
			.join(category)
			.on(squad.category.id.eq(category.id))
			.leftJoin(user)
			.on(userSquad.user.id.eq(user.id).and(userSquad.isCreator.isTrue()))
			.leftJoin(mentorUserSquad)
			.on(userSquad.squad.id.eq(mentorUserSquad.squad.id).and(mentorUserSquad.isMentor.isTrue()))
			.where(squad.squadStatus.eq(SquadStatus.RECRUIT),
				isMentorEq(mentorUserSquad, searchCondition.getMentor()),
				categoryNameEq(searchCondition.getCategoryName()))
			.groupBy(squad.id)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(squad.id.desc())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(squad.count())
			.from(squad)
			.join(userSquad)
			.on(userSquad.squad.id.eq(squad.id))
			.join(category)
			.on(category.id.eq(squad.category.id))
			.leftJoin(mentorUserSquad)
			.on(mentorUserSquad.squad.id.eq(userSquad.squad.id).and(mentorUserSquad.isMentor.isTrue()))
			.where(squad.squadStatus.eq(SquadStatus.RECRUIT),
				isMentorEq(mentorUserSquad, searchCondition.getMentor()),
				categoryNameEq(searchCondition.getCategoryName()));

		return PageableExecutionUtils.getPage(fetch, pageable, countQuery::fetchOne);
	}

	@Override
	public Page<UserSquadResponseDto> getUserSquads(Long userId, Pageable pageable) {
		List<UserSquadResponseDto> fetch = queryFactory
			.select(new QUserSquadResponseDto(
				squad.id,
				squad.squadName,
				squad.squadExplain,
				category.categoryName,
				squad.squadStatus
			))
			.from(squad)
			.join(userSquad).on(squad.id.eq(userSquad.squad.id))
			.join(category).on(squad.category.id.eq(category.id))
			.where(userSquad.user.id.eq(userId))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(squad.id.desc())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(squad.count())
			.from(squad)
			.join(userSquad).on(squad.id.eq(userSquad.squad.id))
			.where(userSquad.user.id.eq(userId));

		return PageableExecutionUtils.getPage(fetch, pageable, countQuery::fetchOne);
	}

	private BooleanExpression categoryNameEq(String categoryName) {
		return !StringUtils.hasText(categoryName) ? null : category.categoryName.eq(categoryName);
	}

	private BooleanExpression isMentorEq(QUserSquad mentorUserSquad, Boolean mentor) {
		if (mentor == null) {
			return null;
		}
		return mentor ? mentorUserSquad.isMentor.isNotNull() : mentorUserSquad.isMentor.isNull();
	}
}
