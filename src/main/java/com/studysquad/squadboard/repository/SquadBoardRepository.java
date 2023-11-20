package com.studysquad.squadboard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studysquad.squadboard.domain.SquadBoard;

public interface SquadBoardRepository extends JpaRepository<SquadBoard, Long>, SquadBoardRepositoryCustom {

	Optional<SquadBoard> findBySquadBoardTitle(String squadBoardTitle);

}
