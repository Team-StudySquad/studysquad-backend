package com.studysquad.boardcomment.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.studysquad.boardcomment.dto.BoardCommentCreateDto;
import com.studysquad.boardcomment.dto.BoardCommentEditDto;
import com.studysquad.boardcomment.dto.BoardCommentResponse;
import com.studysquad.boardcomment.service.BoardCommentService;
import com.studysquad.global.common.SuccessResponse;
import com.studysquad.global.security.Login;
import com.studysquad.user.dto.LoginUser;

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

	@PostMapping("/api/board/{boardId}/boardcomment")
	@ResponseStatus(HttpStatus.CREATED)
	public SuccessResponse<Void> createBoardComment(@PathVariable Long boardId, @Login LoginUser loginUser,
		@RequestBody @Valid BoardCommentCreateDto createRequest) {

		boardCommentService.createBoardComment(boardId, loginUser, createRequest);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.CREATED.value())
			.message("게시글 댓글 생성 성공")
			.build();
	}

	@PatchMapping("/api/board/{boardId}/boardcomment/{boardCommentId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Void> editBoardComment(@PathVariable Long boardId, @PathVariable Long boardCommentId,
		@RequestBody @Valid BoardCommentEditDto editRequest,
		@Login LoginUser loginUser) {

		boardCommentService.editBoardComment(boardId, boardCommentId, editRequest, loginUser);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.OK.value())
			.message("게시글 댓글 수정 성공")
			.build();
	}

	@DeleteMapping("/api/board/{boardId}/boardcomment/{boardCommentId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Void> deleteBoardComment(@PathVariable Long boardId, @PathVariable Long boardCommentId,
		@Login LoginUser loginUser) {

		boardCommentService.deleteBoardComment(boardId, boardCommentId, loginUser);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.OK.value())
			.message("게시글 삭제 성공")
			.build();
	}
}
