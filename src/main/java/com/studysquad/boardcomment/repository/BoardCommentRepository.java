package com.studysquad.boardcomment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studysquad.boardcomment.domain.BoardComment;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long>, BoardCommentRepositoryCustom {
}
