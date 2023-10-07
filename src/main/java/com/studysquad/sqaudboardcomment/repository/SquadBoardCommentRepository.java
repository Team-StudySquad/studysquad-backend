package com.studysquad.sqaudboardcomment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studysquad.sqaudboardcomment.domain.SquadBoardComment;
import com.studysquad.squadboard.domain.SquadBoard;

public interface SquadBoardCommentRepository extends JpaRepository<SquadBoardComment, Long> {

	List<SquadBoardComment> findBySquadBoard(SquadBoard squadBoard);
}
