package com.studysquad.squad.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studysquad.squad.domain.Squad;

public interface SquadRepository extends JpaRepository<Squad, Long>, SquadRepositoryCustom {

}
