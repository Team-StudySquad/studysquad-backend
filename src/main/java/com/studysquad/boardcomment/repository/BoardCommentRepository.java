package com.studysquad.boardcomment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.studysquad.boardcomment.domain.BoardComment;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long>, BoardCommentRepositoryCustom {

	@Query("select c from BoardComment c join fetch c.user join fetch c.board where c.id = :boardCommentId")
	Optional<BoardComment> findByIdWithUserAndBoard(Long boardCommentId);
}
