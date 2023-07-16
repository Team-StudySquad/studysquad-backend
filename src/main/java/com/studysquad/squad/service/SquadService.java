package com.studysquad.squad.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studysquad.category.domain.Category;
import com.studysquad.category.repository.CategoryRepository;
import com.studysquad.global.error.exception.ExistActiveSquadException;
import com.studysquad.global.error.exception.InvalidCategoryException;
import com.studysquad.global.error.exception.MentorAlreadyExistException;
import com.studysquad.global.error.exception.MentorRequiredException;
import com.studysquad.global.error.exception.SquadAlreadyFullException;
import com.studysquad.global.error.exception.SquadNotFoundException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.dto.SquadCreateDto;
import com.studysquad.squad.dto.SquadJoinDto;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;
import com.studysquad.usersquad.domain.UserSquad;
import com.studysquad.usersquad.repository.UserSquadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SquadService {

	private static final int UPDATE_SQUAD_SIZE = 3;
	private static final int MAX_SQUAD_USERS = 4;
	private final UserRepository userRepository;
	private final SquadRepository squadRepository;
	private final UserSquadRepository userSquadRepository;
	private final CategoryRepository categoryRepository;

	@Transactional
	public void createSquad(SquadCreateDto createRequest, LoginUser loginUser) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		if (userSquadRepository.hasActiveSquadByUserId(user.getId())) {
			throw new ExistActiveSquadException();
		}
		Category category = categoryRepository.findByCategoryName(createRequest.getCategoryName())
			.orElseThrow(InvalidCategoryException::new);

		Squad squad = Squad.createSquad(category, createRequest);
		UserSquad userSquad = UserSquad.createUserSquad(user, squad, createRequest.isMentor(), true);

		squadRepository.save(squad);
		userSquadRepository.save(userSquad);
	}

	@Transactional
	public void joinSquad(SquadJoinDto joinRequest, Long squadId, LoginUser loginUser) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		if (userSquadRepository.hasActiveSquadByUserId(user.getId())) {
			throw new ExistActiveSquadException();
		}
		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);
		List<UserSquad> userSquads = userSquadRepository.findBySquadId(squad.getId());

		if (userSquads.size() >= MAX_SQUAD_USERS) {
			throw new SquadAlreadyFullException();
		}
		if (joinRequest.isMentor() && isMentorIncluded(userSquads)) {
			throw new MentorAlreadyExistException();
		}
		if (!joinRequest.isMentor() && isMenteeCountExceeded(userSquads)) {
			throw new MentorRequiredException();
		}
		UserSquad userSquad = UserSquad.createUserSquad(user, squad, joinRequest.isMentor(), false);

		if (userSquads.size() >= UPDATE_SQUAD_SIZE) {
			squad.updateStatus(SquadStatus.PROCESS);
		}
		userSquadRepository.save(userSquad);
	}

	private boolean isMentorIncluded(List<UserSquad> userSquads) {
		long count = userSquads.stream()
			.filter(UserSquad::isMentor)
			.count();
		return count != 0;
	}

	private boolean isMenteeCountExceeded(List<UserSquad> userSquads) {
		long count = userSquads.stream()
			.filter(userSquad -> !userSquad.isMentor())
			.count();
		return count >= 3;
	}
}
