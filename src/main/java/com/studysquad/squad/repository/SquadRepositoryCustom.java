package com.studysquad.squad.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.studysquad.squad.dto.EndSquadDto;
import com.studysquad.squad.dto.ProcessSquadDto;
import com.studysquad.squad.dto.SquadResponseDto;
import com.studysquad.squad.dto.SquadSearchCondition;
import com.studysquad.squad.dto.UserSquadResponseDto;

public interface SquadRepositoryCustom {
	Optional<ProcessSquadDto> getProcessSquad(Long userId);

	Optional<SquadResponseDto> findSquadBySquadId(Long squadId);

	Optional<EndSquadDto> getEndSquad(Long squadId, Long userId);

	Page<SquadResponseDto> searchSquadPageByCondition(SquadSearchCondition searchCondition, Pageable pageable);

	Page<UserSquadResponseDto> getUserSquads(Long userId, Pageable pageable);
}
