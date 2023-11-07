package com.studysquad.sqaudboardcomment.controller;

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

import com.studysquad.global.common.SuccessResponse;
import com.studysquad.global.security.Login;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentCreateDto;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentEditDto;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentResponseDto;
import com.studysquad.sqaudboardcomment.service.SquadBoardCommentService;
import com.studysquad.user.dto.LoginUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SquadBoardCommentController {

	private final SquadBoardCommentService squadBoardCommentService;

	@GetMapping("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomments")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<List<SquadBoardCommentResponseDto>> getSquadBoardComments(
		@Login LoginUser loginUser,
		@PathVariable Long squadId,
		@PathVariable Long squadBoardId) {

		return SuccessResponse.<List<SquadBoardCommentResponseDto>>builder()
			.status(HttpStatus.OK.value())
			.message("스쿼드 게시글 댓글 전체 조회 성공")
			.data(squadBoardCommentService.getSquadBoardComments(loginUser, squadId, squadBoardId))
			.build();
	}

	@PostMapping("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment")
	@ResponseStatus(HttpStatus.CREATED)
	public SuccessResponse<Void> createSquadBoardComment(@RequestBody @Valid SquadBoardCommentCreateDto createDto,
		@Login LoginUser loginUser,
		@PathVariable Long squadId,
		@PathVariable Long squadBoardId) {
		squadBoardCommentService.createSquadBoardComment(loginUser, squadId, squadBoardId, createDto);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.CREATED.value())
			.message("스쿼드 게시글 댓글 생성 성공")
			.build();
	}

	@PatchMapping("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Void> editSquadBoardComment(@RequestBody @Valid SquadBoardCommentEditDto editDto,
		@Login LoginUser loginUser,
		@PathVariable Long squadId,
		@PathVariable Long squadBoardId,
		@PathVariable Long squadBoardCommentId) {
		squadBoardCommentService.editSquadBoardComment(loginUser, squadId, squadBoardId, squadBoardCommentId, editDto);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.OK.value())
			.message("스쿼드 게시글 댓글 수정 성공")
			.build();
	}

	@DeleteMapping("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Void> deleteSquadBoardComment(@Login LoginUser loginUser, @PathVariable Long squadId,
		@PathVariable Long squadBoardId,
		@PathVariable Long squadBoardCommentId) {
		squadBoardCommentService.deleteSquadBoardComment(loginUser, squadId, squadBoardId, squadBoardCommentId);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.OK.value())
			.message("스쿼드 게시글 댓글 삭제 성공")
			.build();
	}
}
