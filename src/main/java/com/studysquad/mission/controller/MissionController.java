package com.studysquad.mission.controller;

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
import com.studysquad.mission.dto.MissionCreateDto;
import com.studysquad.mission.dto.MissionEditDto;
import com.studysquad.mission.dto.MissionResponseDto;
import com.studysquad.mission.service.MissionService;
import com.studysquad.user.dto.LoginUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MissionController {

	private final MissionService missionService;

	@GetMapping("/api/squad/{squadId}/mission/process")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<MissionResponseDto> getProcessMission(@PathVariable Long squadId,
		@Login LoginUser loginUser) {

		return SuccessResponse.<MissionResponseDto>builder()
			.status(HttpStatus.OK.value())
			.message("진행중인 미션 조회 성공")
			.data(missionService.getProcessMission(squadId, loginUser))
			.build();
	}

	@GetMapping("/api/squad/{squadId}/missions")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<List<MissionResponseDto>> getMissions(@PathVariable Long squadId,
		@Login LoginUser loginUser) {

		return SuccessResponse.<List<MissionResponseDto>>builder()
			.status(HttpStatus.OK.value())
			.message("미션 리스트 조회 성공")
			.data(missionService.getMissions(squadId, loginUser))
			.build();
	}

	@PostMapping("/api/squad/{squadId}/mission")
	@ResponseStatus(HttpStatus.CREATED)
	public SuccessResponse<Void> createMission(@PathVariable Long squadId,
		@RequestBody @Valid List<MissionCreateDto> createRequest,
		@Login LoginUser loginUser) {

		missionService.createMission(squadId, createRequest, loginUser);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.CREATED.value())
			.message("미션 생성 성공")
			.build();
	}

	@PatchMapping("/api/squad/{squadId}/mission/{missionId}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse<Void> editMission(@PathVariable Long squadId,
		@PathVariable Long missionId,
		@RequestBody @Valid MissionEditDto editRequest,
		@Login LoginUser loginUser) {

		missionService.editMission(squadId, missionId, editRequest, loginUser);

		return SuccessResponse.<Void>builder()
			.status(HttpStatus.OK.value())
			.message("미션 수정 성공")
			.build();
	}
}
