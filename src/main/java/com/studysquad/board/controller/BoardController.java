package com.studysquad.board.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

	@GetMapping("/api/squad/{squadId}/board/{boardId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<BoardResponse> getBoard(@PathVariable Long boardId) {
		return SuccessResponse.<BoardResponse>builder()
			.status(HttpStatus.OK.value())
			.message("게시글 단건 조회 성공")
			.data(boardService.getBoard(boardId))
			.build();
	}

	@GetMapping("/boards")
	public List<BoardResponse> getAllBoards(@PageableDefault(size = 5) Pageable pageable) {
		return boardService.getAllBoards(pageable);
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
