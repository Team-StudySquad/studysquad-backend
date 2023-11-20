package com.studysquad.squad.controller;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.studysquad.global.common.SuccessResponse;
import com.studysquad.global.security.Login;
import com.studysquad.squad.dto.EndSquadDto;
import com.studysquad.squad.dto.ProcessSquadDto;
import com.studysquad.squad.dto.SquadCreateDto;
import com.studysquad.squad.dto.SquadJoinDto;
import com.studysquad.squad.dto.SquadResponseDto;
import com.studysquad.squad.dto.SquadSearchCondition;
import com.studysquad.squad.dto.UserSquadResponseDto;
import com.studysquad.squad.service.SquadService;
import com.studysquad.user.dto.LoginUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SquadController {

	private final SquadService squadService;

	@GetMapping("/api/squad/{squadId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<SquadResponseDto> getSquad(@PathVariable Long squadId) {

		return SuccessResponse.<SquadResponseDto>builder()
			.status(HttpStatus.OK.value())
			.message("스쿼드 단건 조회 성공")
			.data(squadService.getSquad(squadId))
			.build();
	}

	@GetMapping("/api/squad/process")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<ProcessSquadDto> getProcessSquad(@Login LoginUser loginUser) {
		ProcessSquadDto responseData = squadService.getProcessSquad(loginUser);

		return SuccessResponse.<ProcessSquadDto>builder()
			.status(HttpStatus.OK.value())
			.message("진행중인 스쿼드 조회 성공")
			.data(responseData)
			.build();
	}

	@GetMapping("/api/squad/end/{squadId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<EndSquadDto> getEndSquad(@PathVariable Long squadId, @Login LoginUser loginUser) {
		return SuccessResponse.<EndSquadDto>builder()
			.status(HttpStatus.OK.value())
			.message("종료된 스쿼드 단건 조회 성공")
			.data(squadService.getEndSquad(squadId, loginUser))
			.build();
	}

	@GetMapping("/api/squad/recruit")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Page<SquadResponseDto>> getRecruitSquads(SquadSearchCondition searchCondition,
		Pageable pageable) {

		return SuccessResponse.<Page<SquadResponseDto>>builder()
			.status(HttpStatus.OK.value())
			.message("모집중인 스쿼드 조회 성공")
			.data(squadService.getRecruitSquads(searchCondition, pageable))
			.build();
	}

	@GetMapping("/api/squads")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Page<UserSquadResponseDto>> getUserSquads(@Login LoginUser loginUser,
		Pageable pageable) {

		return SuccessResponse.<Page<UserSquadResponseDto>>builder()
			.status(HttpStatus.OK.value())
			.message("사용자 스쿼드 조회 성공")
			.data(squadService.getUserSquads(loginUser, pageable))
			.build();
	}

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
