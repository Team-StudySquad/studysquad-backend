package com.studysquad.squadboard.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.studysquad.global.common.SuccessResponse;
import com.studysquad.global.security.Login;
import com.studysquad.squadboard.dto.SquadBoardCreateDto;
import com.studysquad.squadboard.dto.SquadBoardEditDto;
import com.studysquad.squadboard.dto.SquadBoardResponseDto;
import com.studysquad.squadboard.service.SquadBoardService;
import com.studysquad.user.dto.LoginUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SquadBoardController {

	private final SquadBoardService squadBoardService;

	@GetMapping("/api/squad/{squadId}/squadboard/{squadBoardId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<SquadBoardResponseDto> getSquadBoard(@Login LoginUser loginUser, @PathVariable Long squadId,
		@PathVariable Long squadBoardId) {

		return SuccessResponse.<SquadBoardResponseDto>builder()
			.status(HttpStatus.OK.value())
			.message("스쿼드 게시글 단건 조회 성공")
			.data(squadBoardService.getSquadBoard(loginUser, squadId, squadBoardId))
			.build();
	}

	@GetMapping("/api/squad/{squadId}/squadboards")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<List<SquadBoardResponseDto>> getSquadBoards(@Login LoginUser loginUser,
		@PathVariable Long squadId) {

		return SuccessResponse.<List<SquadBoardResponseDto>>builder()
			.status(HttpStatus.OK.value())
			.message("스쿼드 게시글 전체 조회 성공")
			.data(squadBoardService.getSquadBoards(loginUser, squadId))
			.build();
	}

	@PostMapping("/api/squad/{squadId}/squadboard")
	@ResponseStatus(HttpStatus.CREATED)
	public SuccessResponse<Void> createSquadBoard(@RequestBody @Valid SquadBoardCreateDto createRequest,
		@Login LoginUser loginUser,
		@PathVariable Long squadId) {
		squadBoardService.createSquadBoard(createRequest, loginUser, squadId);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.CREATED.value())
			.message("스쿼드 게시글 생성 성공")
			.build();
	}

	@PatchMapping("/api/squad/{squadId}/squadboard/{squadBoardId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Void> editSquadBoard(@RequestBody @Valid SquadBoardEditDto editRequest,
		@Login LoginUser loginUser, @PathVariable Long squadId, @PathVariable Long squadBoardId) {

		squadBoardService.editSquadBoard(editRequest, loginUser, squadId, squadBoardId);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.OK.value())
			.message("스쿼드 게시글 수정 성공")
			.build();
	}

}
