package com.studysquad.squadboard.repository;

import static com.studysquad.mission.domain.QMission.*;
import static com.studysquad.squadboard.domain.QSquadBoard.*;
import static com.studysquad.user.domain.QUser.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.squadboard.dto.QSquadBoardResponseDto;
import com.studysquad.squadboard.dto.SquadBoardResponseDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SquadBoardRepositoryImpl implements SquadBoardRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<SquadBoardResponseDto> getSquadBoards(Long squadId) {
		return queryFactory.select(new QSquadBoardResponseDto(
				squadBoard.id,
				mission.missionSequence,
				squadBoard.squadBoardTitle,
				squadBoard.squadBoardContent,
				squadBoard.user.nickname))
			.from(squadBoard)
			.join(mission).on(squadBoard.mission.id.eq(mission.id))
			.join(user).on(user.id.eq(squadBoard.user.id))
			.where(squadBoard.squad.id.eq(squadId))
			.where(mission.missionStatus.notIn(MissionStatus.NOT_PROCESS))
			.fetch();
	}

	@Override
	public Optional<SquadBoardResponseDto> getSquadBoard(Long squadId, Long squadBoardId) {
		SquadBoardResponseDto fetchOne = queryFactory.select(new QSquadBoardResponseDto(
				squadBoard.id,
				mission.missionSequence,
				squadBoard.squadBoardTitle,
				squadBoard.squadBoardContent,
				squadBoard.user.nickname
			))
			.from(squadBoard)
			.join(mission).on(squadBoard.mission.id.eq(mission.id))
			.join(user).on(user.id.eq(squadBoard.user.id))
			.where(
				squadBoard.squad.id.eq(squadId)
					.and(squadBoard.id.eq(squadBoardId)))
			.fetchOne();

		return Optional.ofNullable(fetchOne);
	}

	@Override
	public Boolean hasSquadBoard(Long userId, Long squadBoardId, Long squadId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(squadBoard)
			.where(
				squadBoard.id.eq(squadBoardId)
					.and(squadBoard.user.id.eq(userId))
					.and(squadBoard.squad.id.eq(squadId)))
			.fetchFirst();
		return fetchOne != null;
	}

	@Override
	public Boolean hasSquadBoardByProgressMission(Long userId, Long missionId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(squadBoard)
			.join(mission).on(squadBoard.mission.id.eq(missionId))
			.where(squadBoard.user.id.eq(userId))
			.fetchOne();

		return fetchOne != null;
	}
}
