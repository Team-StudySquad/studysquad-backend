package com.studysquad.mission.repository;

import static com.studysquad.mission.domain.QMission.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.dto.MissionResponseDto;
import com.studysquad.mission.dto.QMissionResponseDto;
import com.studysquad.squadboard.domain.QSquadBoard;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionRepositoryImpl implements MissionRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public Optional<MissionResponseDto> getProcessMission(Long squadId) {
		MissionResponseDto fetchOne = queryFactory.select(new QMissionResponseDto(
				mission.id,
				mission.missionTitle,
				mission.missionContent,
				mission.missionSequence,
				mission.missionStatus))
			.from(mission)
			.where(mission.squad.id.eq(squadId)
				.and(mission.missionStatus.eq(MissionStatus.PROCESS)))
			.fetchOne();

		return Optional.ofNullable(fetchOne);
	}

	@Override
	public List<MissionResponseDto> getMissions(Long squadId) {
		return queryFactory.select(new QMissionResponseDto(
				mission.id,
				mission.missionTitle,
				mission.missionContent,
				mission.missionSequence,
				mission.missionStatus))
			.from(mission)
			.where(mission.squad.id.eq(squadId))
			.fetch();
	}

	@Override
	public Optional<Mission> getProcessMissionEntity(Long squadId) {
		Mission fetchOne = queryFactory.selectFrom(mission)
			.where(mission.squad.id.eq(squadId).and(mission.missionStatus.eq(MissionStatus.PROCESS)))
			.fetchOne();

		return Optional.ofNullable(fetchOne);
	}

	@Override
	public Optional<Long> hasSquadBoardByMissionId(Long missionId) {
		Long count = queryFactory.select(mission.id.count())
			.from(mission)
			.join(QSquadBoard.squadBoard).on(mission.id.eq(QSquadBoard.squadBoard.mission.id))
			.where(mission.id.eq(missionId))
			.groupBy(mission.id)
			.fetchFirst();

		return Optional.ofNullable(count);
	}

	@Override
	public Optional<Mission> getNextMission(Long squadId, int sequence) {
		Mission fetchOne = queryFactory.selectFrom(mission)
			.where(mission.missionSequence.eq(sequence + 1)
				.and(mission.squad.id.eq(squadId)))
			.fetchOne();

		return Optional.ofNullable(fetchOne);
	}
}
