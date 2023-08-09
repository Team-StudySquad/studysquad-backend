package com.studysquad.mission.repository;

import java.util.List;

import com.studysquad.mission.dto.MissionResponseDto;

public interface MissionRepositoryCustom {

	List<MissionResponseDto> getMissions(Long squadId);
}
