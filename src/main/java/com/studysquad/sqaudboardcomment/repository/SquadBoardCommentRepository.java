package com.studysquad.sqaudboardcomment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studysquad.sqaudboardcomment.domain.SquadBoardComment;

public interface SquadBoardCommentRepository
	extends JpaRepository<SquadBoardComment, Long>, SquadBoardCommentRepositoryCustom {
}
