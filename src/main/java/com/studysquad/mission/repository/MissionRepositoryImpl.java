package com.studysquad.mission.repository;

import static com.studysquad.mission.domain.QMission.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.dto.MissionResponseDto;
import com.studysquad.mission.dto.QMissionResponseDto;

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
}
