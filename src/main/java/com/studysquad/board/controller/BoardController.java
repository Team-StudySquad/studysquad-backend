package com.studysquad.board.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.request.BoardEdit;
import com.studysquad.board.request.BoardSearchCondition;
import com.studysquad.board.response.BoardResponse;
import com.studysquad.board.service.BoardService;
import com.studysquad.global.common.SuccessResponse;
import com.studysquad.global.security.Login;
import com.studysquad.user.dto.LoginUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BoardController {

	private final BoardService boardService;

	@GetMapping("/api/board/{boardId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<BoardResponse> getBoard(@PathVariable Long boardId) {
		return SuccessResponse.<BoardResponse>builder()
			.status(HttpStatus.OK.value())
			.message("게시글 단건 조회 성공")
			.data(boardService.getBoard(boardId))
			.build();
	}

	@GetMapping("/api/boards")
	public SuccessResponse<Page<BoardResponse>> getBoards(BoardSearchCondition searchCondition, Pageable pageable) {
		return SuccessResponse.<Page<BoardResponse>>builder()
			.status(HttpStatus.OK.value())
			.message("게시글 페이징 조회 성공")
			.data(boardService.getBoards(searchCondition, pageable))
			.build();
	}

	@GetMapping("/api/squad/{squadId}/board/allowed")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Boolean> isBoardAllowed(@PathVariable Long squadId, @Login LoginUser loginUser) {
		return SuccessResponse.<Boolean>builder()
			.status(HttpStatus.OK.value())
			.message("게시글 작성 가능 조회 성공")
			.data(boardService.isBoardAllowed(squadId, loginUser))
			.build();
	}

	@GetMapping("/api/squad/{squadId}/boards")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<List<BoardResponse>> getBoardsWithSquad(@PathVariable Long squadId,
		@Login LoginUser loginUser) {

		return SuccessResponse.<List<BoardResponse>>builder()
			.status(HttpStatus.OK.value())
			.message("스쿼드 전체 게시글 리스트 조회 성공")
			.data(boardService.getBoardsWithSquad(squadId, loginUser))
			.build();
	}

	@PostMapping("/api/squad/{squadId}/board")
	@ResponseStatus(HttpStatus.CREATED)
	public SuccessResponse<Void> board(@RequestBody @Valid BoardCreate boardCreate,
		@PathVariable Long squadId,
		@Login LoginUser loginUser) {

		boardService.createBoard(boardCreate, squadId, loginUser);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.CREATED.value())
			.message("게시글 작성 성공")
			.build();
	}

	@PatchMapping("/api/squad/{squadId}/board/{boardId}")
	public SuccessResponse<Void> edit(@PathVariable Long boardId,
		@PathVariable Long squadId,
		@RequestBody @Valid BoardEdit request,
		@Login LoginUser loginuser
	) {
		boardService.edit(boardId, squadId, request, loginuser);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.OK.value())
			.message("게시글 수정 성공")
			.build();
	}

	@DeleteMapping("/api/squad/{squadId}/board/{boardId}")
	public SuccessResponse<Void> delete(
		@PathVariable Long boardId,
		@PathVariable Long squadId,
		@Login LoginUser loginuser
	) {
		boardService.delete(boardId, squadId, loginuser);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.OK.value())
			.message("게시글 삭제 성공")
			.build();
	}

}
