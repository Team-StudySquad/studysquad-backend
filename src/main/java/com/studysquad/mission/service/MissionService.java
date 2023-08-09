package com.studysquad.mission.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studysquad.global.error.exception.MissionNotFoundException;
import com.studysquad.global.error.exception.NotMentorException;
import com.studysquad.global.error.exception.NotSquadUserException;
import com.studysquad.global.error.exception.ProcessMissionException;
import com.studysquad.global.error.exception.SquadNotFoundException;
import com.studysquad.global.error.exception.SquadNotProgressException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.dto.MissionCreateDto;
import com.studysquad.mission.dto.MissionEditDto;
import com.studysquad.mission.dto.MissionResponseDto;
import com.studysquad.mission.repository.MissionRepository;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {

	private final MissionRepository missionRepository;
	private final UserRepository userRepository;
	private final SquadRepository squadRepository;

	public List<MissionResponseDto> getMissions(Long squadId, LoginUser loginUser) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);
		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		validateUserIsMemberOfSquad(squad, user);

		return missionRepository.getMissions(squad.getId());
	}

	@Transactional
	public void createMission(Long squadId, List<MissionCreateDto> createRequest, LoginUser loginUser) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);
		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		validateProcessSquad(squad);
		validateMentorOfSquad(squad.getId(), user.getId());

		List<Mission> missions = createMissions(squad, createRequest);
		missionRepository.saveAll(missions);
	}

	@Transactional
	public void editMission(Long squadId, Long missionId, MissionEditDto editRequest, LoginUser loginUser) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);
		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		validateProcessSquad(squad);
		validateMentorOfSquad(squad.getId(), user.getId());

		Mission mission = missionRepository.findById(missionId)
			.orElseThrow(MissionNotFoundException::new);

		if (!mission.getMissionStatus().equals(MissionStatus.NOT_PROCESS)) {
			throw new ProcessMissionException();
		}
		mission.edit(editRequest);
	}

	private void validateUserIsMemberOfSquad(Squad squad, User user) {
		if (!squadRepository.isUserOfSquad(squad.getId(), user.getId())) {
			throw new NotSquadUserException();
		}
	}

	private void validateProcessSquad(Squad squad) {
		if (!squad.getSquadStatus().equals(SquadStatus.PROCESS)) {
			throw new SquadNotProgressException();
		}
	}

	private void validateMentorOfSquad(Long squadId, Long userId) {
		if (!squadRepository.isMentorOfSquad(squadId, userId)) {
			throw new NotMentorException();
		}
	}

	private List<Mission> createMissions(Squad squad, List<MissionCreateDto> createRequest) {
		return createRequest.stream()
			.map(dto -> Mission.builder()
				.squad(squad)
				.missionTitle(dto.getMissionTitle())
				.missionContent(dto.getMissionContent())
				.missionSequence(dto.getMissionSequence())
				.missionStatus(getMissionStatus(dto.getMissionSequence()))
				.build())
			.collect(Collectors.toList());
	}

	private MissionStatus getMissionStatus(int missionSequence) {
		return missionSequence == 0 ? MissionStatus.PROCESS : MissionStatus.NOT_PROCESS;
	}
}
