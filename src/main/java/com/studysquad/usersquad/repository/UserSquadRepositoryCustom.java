package com.studysquad.usersquad.repository;

public interface UserSquadRepositoryCustom {
	Boolean hasActiveSquadByUserId(Long userId);

	Boolean hasMentorBySquadId(Long squadId);
}
