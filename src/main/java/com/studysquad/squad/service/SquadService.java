package com.studysquad.squad.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studysquad.category.domain.Category;
import com.studysquad.category.repository.CategoryRepository;
import com.studysquad.global.error.exception.ExistActiveSquadException;
import com.studysquad.global.error.exception.InvalidCategoryException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.dto.SquadCreateDto;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;
import com.studysquad.usersquad.domain.UserSquad;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SquadService {

	private final UserRepository userRepository;
	private final SquadRepository squadRepository;
	private final CategoryRepository categoryRepository;

	@Transactional
	public void createSquad(SquadCreateDto squadCreateDto, LoginUser loginUser) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		if (squadRepository.isUserInActiveSquad(user.getId())) {
			throw new ExistActiveSquadException();
		}

		Category category = categoryRepository.findByCategoryName(squadCreateDto.getCategoryName())
			.orElseThrow(InvalidCategoryException::new);

		Squad squad = Squad.createSquad(category, squadCreateDto);

		UserSquad userSquad = UserSquad.createUserSquad(user, squad, squadCreateDto.isMentor());
		squad.addUserSquad(userSquad);

		squadRepository.save(squad);
	}
}
