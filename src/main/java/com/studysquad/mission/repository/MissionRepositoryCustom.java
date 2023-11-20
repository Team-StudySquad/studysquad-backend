package com.studysquad.mission.repository;

import java.util.List;
import java.util.Optional;

import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.dto.MissionResponseDto;

public interface MissionRepositoryCustom {

	Optional<MissionResponseDto> getProcessMission(Long squadId);

	List<MissionResponseDto> getMissions(Long squadId);

	Optional<Mission> getProcessMissionEntity(Long squadId);

	Optional<Long> hasSquadBoardByMissionId(Long missionId);

	Optional<Mission> getNextMission(Long squadId, int sequence);

}
