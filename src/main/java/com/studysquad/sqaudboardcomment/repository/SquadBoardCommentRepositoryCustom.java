package com.studysquad.sqaudboardcomment.repository;

import java.util.List;

import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentResponseDto;

public interface SquadBoardCommentRepositoryCustom {

	Boolean isUserOfSquadBoardComment(Long userId, Long squadBoardId, Long commentId);

	List<SquadBoardCommentResponseDto> getSquadBoardComments(Long squadBoardId);
}
