package com.studysquad.squad.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.studysquad.squad.dto.ProcessSquadDto;
import com.studysquad.squad.dto.SquadResponseDto;
import com.studysquad.squad.dto.SquadSearchCondition;

public interface SquadRepositoryCustom {
	Optional<ProcessSquadDto> getProcessSquad(Long userId);

	Page<SquadResponseDto> searchSquadPageByCondition(SquadSearchCondition searchCondition, Pageable pageable);
}
