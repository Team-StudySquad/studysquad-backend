package com.studysquad.squad.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.studysquad.global.common.SuccessResponse;
import com.studysquad.global.security.Login;
import com.studysquad.squad.dto.SquadCreateDto;
import com.studysquad.squad.dto.SquadJoinDto;
import com.studysquad.squad.service.SquadService;
import com.studysquad.user.dto.LoginUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SquadController {

	private final SquadService squadService;

	@PostMapping("/api/squad")
	@ResponseStatus(HttpStatus.CREATED)
	public SuccessResponse<Void> createSquad(@RequestBody @Valid SquadCreateDto createRequest,
		@Login LoginUser loginUser) {

		squadService.createSquad(createRequest, loginUser);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.CREATED.value())
			.message("스쿼드 생성 성공")
			.build();
	}

	@PostMapping("/api/squad/{squadId}/join")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Void> joinSquad(@RequestBody @Valid SquadJoinDto joinRequest,
		@PathVariable("squadId") Long squadId,
		@Login LoginUser loginUser) {

		squadService.joinSquad(joinRequest, squadId, loginUser);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.OK.value())
			.message("스쿼드 가입 성공")
			.build();
	}
}
