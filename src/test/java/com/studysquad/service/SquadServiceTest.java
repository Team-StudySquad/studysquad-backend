package com.studysquad.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studysquad.category.domain.Category;
import com.studysquad.category.repository.CategoryRepository;
import com.studysquad.global.error.exception.ExistActiveSquadException;
import com.studysquad.global.error.exception.InvalidCategoryException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.dto.SquadCreateDto;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.squad.service.SquadService;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class SquadServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	SquadRepository squadRepository;
	@Mock
	CategoryRepository categoryRepository;
	@InjectMocks
	SquadService squadService;

	@Test
	@DisplayName("스쿼드 생성")
	void successSquadCreate() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user.getEmail());
		Category category = createCategory();
		SquadCreateDto squadCreateDto = createSquadCreateDto(category.getCategoryName());

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.isUserInActiveSquad(user.getId()))
			.thenReturn(false);
		when(categoryRepository.findByCategoryName(squadCreateDto.getCategoryName()))
			.thenReturn(Optional.of(category));

		squadService.createSquad(squadCreateDto, loginUser);

		verify(userRepository).findByEmail(loginUser.getEmail());
		verify(squadRepository).isUserInActiveSquad(user.getId());
		verify(categoryRepository).findByCategoryName(squadCreateDto.getCategoryName());
		verify(squadRepository).save(any(Squad.class));
	}

	@Test
	@DisplayName("스쿼드 생성 시 로그인한 유저가 유효하지 않음")
	void failCreateSquadUserNotFound() {
		SquadCreateDto squadCreateDto = createSquadCreateDto("Java");
		LoginUser loginUser = createLoginUser("aaa@aaa.com");

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.createSquad(squadCreateDto, loginUser))
			.isInstanceOf(UserNotFoundException.class)
			.message().isEqualTo("사용자를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("스쿼드 생성 시 이미 진행중인 스쿼드가 존재")
	void failCreateSquadAlreadyExistProgressSquad() {
		User user = createUser();
		SquadCreateDto squadCreateDto = createSquadCreateDto("Java");
		LoginUser loginUser = createLoginUser("aaa@aaa.com");

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.isUserInActiveSquad(user.getId()))
			.thenReturn(true);

		assertThatThrownBy(() -> squadService.createSquad(squadCreateDto, loginUser))
			.isInstanceOf(ExistActiveSquadException.class)
			.message().isEqualTo("이미 진행중인 스쿼드가 존재 합니다");
	}

	@Test
	@DisplayName("스쿼드 생성 시 카테고리 이름이 유효하지 않음")
	void failCreateSquadCategoryNotFound() {
		User user = createUser();
		SquadCreateDto squadCreateDto = createSquadCreateDto("invalidName");
		LoginUser loginUser = createLoginUser("aaa@aaa.com");

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.isUserInActiveSquad(user.getId()))
			.thenReturn(false);
		when(categoryRepository.findByCategoryName(squadCreateDto.getCategoryName()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.createSquad(squadCreateDto, loginUser))
			.isInstanceOf(InvalidCategoryException.class)
			.message().isEqualTo("유효하지 않은 카테고리 입니다");
	}

	private static SquadCreateDto createSquadCreateDto(String categoryName) {
		return SquadCreateDto.builder()
			.categoryName(categoryName)
			.squadName("squadName")
			.squadExplain("this squad ...")
			.isMentor(true)
			.build();
	}

	private static Category createCategory() {
		return Category.builder()
			.categoryName("Java")
			.build();
	}

	private static LoginUser createLoginUser(String email) {
		return LoginUser.builder()
			.email(email)
			.role(Role.USER)
			.build();
	}

	private static User createUser() {
		return User.builder()
			.email("aaa@aaa.com")
			.nickname("nickname")
			.password("password")
			.role(Role.USER)
			.build();
	}
}
