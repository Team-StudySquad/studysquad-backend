package com.studysquad.mission.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.studysquad.global.common.SuccessResponse;
import com.studysquad.global.security.Login;
import com.studysquad.mission.dto.MissionCreateDto;
import com.studysquad.mission.service.MissionService;
import com.studysquad.user.dto.LoginUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MissionController {

	private final MissionService missionService;

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
}
