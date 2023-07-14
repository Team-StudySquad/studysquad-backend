package com.studysquad.usersquad.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studysquad.usersquad.domain.UserSquad;

public interface UserSquadRepository extends JpaRepository<UserSquad, Long>, UserSquadRepositoryCustom {
}
