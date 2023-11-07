package com.studysquad.squadboard.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studysquad.global.error.exception.ExistSquadBoardByProcessMission;
import com.studysquad.global.error.exception.NotFoundProcessMission;
import com.studysquad.global.error.exception.NotFoundSquadBoard;
import com.studysquad.global.error.exception.NotMenteeException;
import com.studysquad.global.error.exception.NotSquadUserException;
import com.studysquad.global.error.exception.SquadNotFoundException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.repository.MissionRepository;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.squadboard.dto.SquadBoardCreateDto;
import com.studysquad.squadboard.dto.SquadBoardEditDto;
import com.studysquad.squadboard.dto.SquadBoardResponseDto;
import com.studysquad.squadboard.repository.SquadBoardRepository;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SquadBoardService {

	private final SquadBoardRepository squadBoardRepository;
	private final UserRepository userRepository;
	private final SquadRepository squadRepository;
	private final MissionRepository missionRepository;

	public SquadBoardResponseDto getSquadBoard(LoginUser loginUser, Long squadId, Long squadBoardId) {

		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		if (!squadRepository.isUserOfSquad(squad.getId(), user.getId())) {
			throw new NotSquadUserException();
		}

		return squadBoardRepository.getSquadBoard(squad.getId(), squadBoardId)
			.orElseThrow(NotFoundSquadBoard::new);

	}

	public List<SquadBoardResponseDto> getSquadBoards(LoginUser loginUser, Long squadId) {

		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		if (!squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			throw new NotSquadUserException();

		return squadBoardRepository.getSquadBoards(squad.getId());
	}

	@Transactional
	public void createSquadBoard(SquadBoardCreateDto squadBoardDto, LoginUser loginUser, Long squadId) {

		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		if (squadRepository.isMentorOfSquad(squad.getId(), user.getId())) {
			throw new NotMenteeException();
		}

		Mission processMission = missionRepository.getProcessMissionEntity(squad.getId())
			.orElseThrow(NotFoundProcessMission::new);

		if (squadBoardRepository.hasSquadBoardByProgressMission(user.getId(), processMission.getId())) {
			throw new ExistSquadBoardByProcessMission();
		}

		squadBoardRepository.save(squadBoardDto.toEntity(squad, user, processMission));
	}

	@Transactional
	public void editSquadBoard(SquadBoardEditDto requestDto, LoginUser loginUser, Long squadId,
		Long squadBoardId) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		SquadBoard squadBoard = squadBoardRepository.findById(squadBoardId)
			.orElseThrow(NotFoundSquadBoard::new);

		if (!squadBoardRepository.hasSquadBoard(user.getId(), squadBoard.getId(), squad.getId())) {
			throw new NotFoundSquadBoard();
		}

		squadBoard.edit(requestDto.getSquadBoardTitle(), requestDto.getSquadBoardContent());
	}
}
