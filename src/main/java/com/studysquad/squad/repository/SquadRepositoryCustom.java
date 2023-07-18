package com.studysquad.squad.repository;

import java.util.Optional;

import com.studysquad.squad.dto.ProcessSquadDto;

public interface SquadRepositoryCustom {
	Optional<ProcessSquadDto> getProcessSquad(Long userId);
}
