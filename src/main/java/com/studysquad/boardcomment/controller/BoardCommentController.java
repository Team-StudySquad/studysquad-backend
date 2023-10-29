package com.studysquad.boardcomment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.studysquad.boardcomment.dto.BoardCommentResponse;
import com.studysquad.boardcomment.service.BoardCommentService;
import com.studysquad.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BoardCommentController {

	private final BoardCommentService boardCommentService;

	@GetMapping("/api/board/{boardId}/boardcomments")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<List<BoardCommentResponse>> getBoardComments(@PathVariable Long boardId) {

		return SuccessResponse.<List<BoardCommentResponse>>builder()
			.status(HttpStatus.OK.value())
			.message("게시글 댓글 조회 성공")
			.data(boardCommentService.getBoardComments(boardId))
			.build();
	}
}
