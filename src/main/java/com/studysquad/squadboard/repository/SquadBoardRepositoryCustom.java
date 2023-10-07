package com.studysquad.squadboard.repository;

import java.util.List;
import java.util.Optional;

import com.studysquad.squadboard.dto.SquadBoardResponseDto;

public interface SquadBoardRepositoryCustom {

	Boolean hasSquadBoard(Long userId, Long squadBoardId, Long squadId);

	Optional<SquadBoardResponseDto> getSquadBoard(Long squadId, Long squadBoardId);

	List<SquadBoardResponseDto> getSquadBoards(Long squadId);

	Boolean hasSquadBoardByProgressMission(Long userId, Long missionId);
}
