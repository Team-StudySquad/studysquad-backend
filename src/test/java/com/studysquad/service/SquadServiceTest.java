package com.studysquad.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.studysquad.category.domain.Category;
import com.studysquad.category.repository.CategoryRepository;
import com.studysquad.global.error.exception.ExistActiveSquadException;
import com.studysquad.global.error.exception.InvalidCategoryException;
import com.studysquad.global.error.exception.MentorAlreadyExistException;
import com.studysquad.global.error.exception.MentorRequiredException;
import com.studysquad.global.error.exception.NotFoundProcessSquad;
import com.studysquad.global.error.exception.SquadAlreadyFullException;
import com.studysquad.global.error.exception.SquadNotFoundException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.dto.ProcessSquadDto;
import com.studysquad.squad.dto.SquadCreateDto;
import com.studysquad.squad.dto.SquadJoinDto;
import com.studysquad.squad.dto.SquadResponseDto;
import com.studysquad.squad.dto.SquadSearchCondition;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.squad.service.SquadService;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;
import com.studysquad.usersquad.domain.UserSquad;
import com.studysquad.usersquad.repository.UserSquadRepository;

@ExtendWith(MockitoExtension.class)
public class SquadServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private SquadRepository squadRepository;
	@Mock
	UserSquadRepository userSquadRepository;
	@Mock
	CategoryRepository categoryRepository;
	@InjectMocks
	SquadService squadService;

	@Test
	@DisplayName("진행중인 스쿼드 조회 성공")
	void successGetProcessSquad() {
		User user = createUser("aaa@aaa.com", "nickname1");
		LoginUser loginUser = createLoginUser(user.getEmail());
		ProcessSquadDto processSquadDto = ProcessSquadDto.builder()
			.squadId(1L)
			.categoryName("Java")
			.squadName("Happy Java Squad")
			.squadExplain("This squad study for java")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.getProcessSquad(user.getId()))
			.thenReturn(Optional.of(processSquadDto));

		squadService.getProcessSquad(loginUser);

		verify(userRepository).findByEmail(loginUser.getEmail());
		verify(squadRepository).getProcessSquad(user.getId());
	}

	@Test
	@DisplayName("유효하지 않은 사용자가 진행중인 스쿼드 조회 요청")
	void failGetProcessSquadInvalidUser() {
		User user = createUser("aaa@aaa.com", "nickname1");
		LoginUser loginUser = createLoginUser(user.getEmail());

		when(userRepository.findByEmail(user.getEmail()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.getProcessSquad(loginUser))
			.isInstanceOf(UserNotFoundException.class)
			.message().isEqualTo("사용자를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("진행중인 스쿼드가 존재하지 않음")
	void failGetProcessSquadNotFoundProcessSquad() {
		User user = createUser("aaa@aaa.com", "nickname1");
		LoginUser loginUser = createLoginUser(user.getEmail());

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.getProcessSquad(user.getId()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.getProcessSquad(loginUser))
			.isInstanceOf(NotFoundProcessSquad.class)
			.message().isEqualTo("진행중인 스쿼드를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("모집중인 스쿼드 조회 성공")
	void successGetRecruitSquads() {
		SquadSearchCondition cond = SquadSearchCondition.builder()
			.build();
		PageRequest page = PageRequest.of(0, 10);

		List<SquadResponseDto> testData = LongStream.range(1, 31)
			.mapToObj(i -> SquadResponseDto.builder()
				.squadId(i)
				.userCount((long)3)
				.squadName("스쿼드 이름 " + i)
				.squadExplain("스쿼드 설명 " + i)
				.categoryName("카테고리 명" + i)
				.build())
			.collect(Collectors.toList());

		List<SquadResponseDto> expectedData = testData.subList(page.getPageNumber(), page.getPageSize());
		Page<SquadResponseDto> expectedPage = new PageImpl<>(expectedData, page, expectedData.size());

		when(squadRepository.searchSquadPageByCondition(cond, page))
			.thenReturn(expectedPage);

		Page<SquadResponseDto> responseData = squadService.getRecruitSquads(cond, page);

		verify(squadRepository, times(1)).searchSquadPageByCondition(eq(cond), eq(page));
		assertThat(responseData.getContent())
			.hasSize(10)
			.isEqualTo(expectedData);
	}

	@Test
	@DisplayName("스쿼드 생성")
	void successSquadCreate() {
		User user = createUser("aaa@aaa.com", "nickname1");
		LoginUser loginUser = createLoginUser(user.getEmail());
		Category category = createCategory();
		SquadCreateDto createRequest = createSquadCreateDto(category.getCategoryName());

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(userSquadRepository.hasActiveSquadByUserId(user.getId()))
			.thenReturn(false);
		when(categoryRepository.findByCategoryName(createRequest.getCategoryName()))
			.thenReturn(Optional.of(category));

		squadService.createSquad(createRequest, loginUser);

		verify(userRepository).findByEmail(loginUser.getEmail());
		verify(userSquadRepository).hasActiveSquadByUserId(user.getId());
		verify(categoryRepository).findByCategoryName(createRequest.getCategoryName());
		verify(userSquadRepository).save(any(UserSquad.class));
		verify(squadRepository).save(any(Squad.class));
	}

	@Test
	@DisplayName("스쿼드 생성 시 로그인한 유저가 유효하지 않음")
	void failCreateSquadUserNotFound() {
		SquadCreateDto createRequest = createSquadCreateDto("Java");
		LoginUser loginUser = createLoginUser("aaa@aaa.com");

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.createSquad(createRequest, loginUser))
			.isInstanceOf(UserNotFoundException.class)
			.message().isEqualTo("사용자를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("스쿼드 생성 시 이미 활성화된 스쿼드가 존재")
	void failCreateSquadAlreadyExistProgressSquad() {
		User user = createUser("aaa@aaa.com", "nickname1");
		SquadCreateDto createRequest = createSquadCreateDto("Java");
		LoginUser loginUser = createLoginUser("aaa@aaa.com");

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(userSquadRepository.hasActiveSquadByUserId(user.getId()))
			.thenReturn(true);

		assertThatThrownBy(() -> squadService.createSquad(createRequest, loginUser))
			.isInstanceOf(ExistActiveSquadException.class)
			.message().isEqualTo("이미 활성화된 스쿼드가 존재 합니다");
	}

	@Test
	@DisplayName("스쿼드 생성 시 카테고리 이름이 유효하지 않음")
	void failCreateSquadCategoryNotFound() {
		User user = createUser("aaa@aaa.com", "nickname1");
		SquadCreateDto createRequest = createSquadCreateDto("invalidName");
		LoginUser loginUser = createLoginUser("aaa@aaa.com");

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(userSquadRepository.hasActiveSquadByUserId(user.getId()))
			.thenReturn(false);
		when(categoryRepository.findByCategoryName(createRequest.getCategoryName()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.createSquad(createRequest, loginUser))
			.isInstanceOf(InvalidCategoryException.class)
			.message().isEqualTo("유효하지 않은 카테고리 입니다");
	}

	@Test
	@DisplayName("스쿼드 가입 성공")
	void successJoinSquad() {
		User user = createUser("aaa@aaa.com", "nickname1");
		User joinUser = createUser("bbb@bbb.com", "nickname2");

		Category category = createCategory();
		SquadCreateDto createDto = createSquadCreateDto(category.getCategoryName());
		Squad squad = Squad.createSquad(category, createDto);

		UserSquad userSquad = UserSquad.createUserSquad(user, squad, true, true);
		UserSquad joinUserSquad = UserSquad.createUserSquad(user, squad, false, false);

		LoginUser loginUser = createLoginUser(joinUser.getEmail());
		SquadJoinDto joinRequest = createJoinDto(false);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(joinUser));
		when(userSquadRepository.hasActiveSquadByUserId(joinUser.getId()))
			.thenReturn(false);
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(userSquadRepository.findBySquadId(squad.getId()))
			.thenReturn(Arrays.asList(userSquad));
		when(userSquadRepository.save(any(UserSquad.class)))
			.thenReturn(joinUserSquad);

		squadService.joinSquad(joinRequest, squad.getId(), loginUser);

		verify(userSquadRepository, times(1)).save(any(UserSquad.class));
	}

	@Test
	@DisplayName("스쿼드 인원이 모두 모집되면 스쿼드 상태를 PROCESS로 변경")
	void successJoinSquadChangeToProcessWhenAllMembersRecruited() {
		User user1 = createUser("aaa@aaa.com", "nickname1");
		User user2 = createUser("bbb@bbb.com", "nickname2");
		User user3 = createUser("ccc@ccc.com", "nickname3");

		User joinUser = createUser("eee@eee.com", "nickname4");

		Category category = createCategory();
		SquadCreateDto createDto = createSquadCreateDto(category.getCategoryName());
		Squad squad = Squad.createSquad(category, createDto);

		UserSquad userSquad1 = UserSquad.createUserSquad(user1, squad, true, true);
		UserSquad userSquad2 = UserSquad.createUserSquad(user2, squad, false, false);
		UserSquad userSquad3 = UserSquad.createUserSquad(user3, squad, false, false);
		UserSquad joinUserSquad = UserSquad.createUserSquad(joinUser, squad, false, false);

		LoginUser loginUser = createLoginUser(joinUser.getEmail());
		SquadJoinDto joinRequest = createJoinDto(false);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(joinUser));
		when(userSquadRepository.hasActiveSquadByUserId(joinUser.getId()))
			.thenReturn(false);
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(userSquadRepository.findBySquadId(squad.getId()))
			.thenReturn(Arrays.asList(userSquad1, userSquad2, userSquad3));
		when(userSquadRepository.save(any(UserSquad.class)))
			.thenReturn(joinUserSquad);

		squadService.joinSquad(joinRequest, squad.getId(), loginUser);

		assertThat(squad.getSquadState()).isEqualTo(SquadStatus.PROCESS);
		verify(userSquadRepository, times(1)).save(any(UserSquad.class));
	}

	@Test
	@DisplayName("스쿼드 가입 시 활성화 된 스쿼드가 존재")
	void failJoinSquadAlreadyActiveSquad() {
		User user = createUser("aaa@aaa.com", "nickname1");

		Category category = createCategory();
		SquadCreateDto createDto = createSquadCreateDto(category.getCategoryName());
		Squad squad = Squad.createSquad(category, createDto);

		LoginUser loginUser = createLoginUser(user.getEmail());
		SquadJoinDto joinRequest = createJoinDto(false);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(userSquadRepository.hasActiveSquadByUserId(user.getId()))
			.thenReturn(true);

		assertThatThrownBy(() -> squadService.joinSquad(joinRequest, squad.getId(), loginUser))
			.isInstanceOf(ExistActiveSquadException.class)
			.message().isEqualTo("이미 활성화된 스쿼드가 존재 합니다");
	}

	@Test
	@DisplayName("존재하지 않는 스쿼드로 가입 신청")
	void failJoinSquadWithNotFoundSquad() {
		User joinUser = createUser("bbb@bbb.com", "nickname2");

		Long notFoundSquadId = 100L;
		LoginUser loginUser = createLoginUser(joinUser.getEmail());
		SquadJoinDto joinRequest = createJoinDto(false);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(joinUser));
		when(userSquadRepository.hasActiveSquadByUserId(joinUser.getId()))
			.thenReturn(false);
		when(squadRepository.findById(notFoundSquadId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.joinSquad(joinRequest, notFoundSquadId, loginUser))
			.isInstanceOf(SquadNotFoundException.class)
			.message().isEqualTo("존재하지 않는 스쿼드 입니다");
	}

	@Test
	@DisplayName("모집완료된 스쿼드에 가입 신청")
	void failJoinSquadAlreadyRecruitCompletedSquad() {
		User user1 = createUser("aaa@aaa.com", "nickname1");
		User user2 = createUser("bbb@bbb.com", "nickname2");
		User user3 = createUser("ccc@ccc.com", "nickname3");
		User user4 = createUser("ddd@ddd.com", "nickname4");

		User joinUser = createUser("eee@eee.com", "nickname5");

		Category category = createCategory();
		SquadCreateDto createDto = createSquadCreateDto(category.getCategoryName());
		Squad squad = Squad.createSquad(category, createDto);

		UserSquad userSquad1 = UserSquad.createUserSquad(user1, squad, true, true);
		UserSquad userSquad2 = UserSquad.createUserSquad(user2, squad, false, false);
		UserSquad userSquad3 = UserSquad.createUserSquad(user3, squad, false, false);
		UserSquad userSquad4 = UserSquad.createUserSquad(user4, squad, false, false);

		LoginUser loginUser = createLoginUser(joinUser.getEmail());
		SquadJoinDto joinRequest = createJoinDto(false);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(joinUser));
		when(userSquadRepository.hasActiveSquadByUserId(joinUser.getId()))
			.thenReturn(false);
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(userSquadRepository.findBySquadId(squad.getId()))
			.thenReturn(Arrays.asList(userSquad1, userSquad2, userSquad3, userSquad4));

		assertThatThrownBy(() -> squadService.joinSquad(joinRequest, squad.getId(), loginUser))
			.isInstanceOf(SquadAlreadyFullException.class)
			.message().isEqualTo("모집이 완료된 스쿼드 입니다");
	}

	@Test
	@DisplayName("멘토가 있는 스쿼드에 멘토로 가입")
	void failJoinSquadMentorAlreadyExist() {
		User user = createUser("aaa@aaa.com", "nickname1");
		User joinUser = createUser("bbb@bbb.com", "nickname2");

		Category category = createCategory();
		SquadCreateDto createDto = createSquadCreateDto(category.getCategoryName());
		Squad squad = Squad.createSquad(category, createDto);

		UserSquad userSquad = UserSquad.createUserSquad(user, squad, true, true);

		LoginUser loginUser = createLoginUser(joinUser.getEmail());
		SquadJoinDto joinRequest = createJoinDto(true);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(joinUser));
		when(userSquadRepository.hasActiveSquadByUserId(joinUser.getId()))
			.thenReturn(false);
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(userSquadRepository.findBySquadId(squad.getId()))
			.thenReturn(Arrays.asList(userSquad));

		assertThatThrownBy(() -> squadService.joinSquad(joinRequest, squad.getId(), loginUser))
			.isInstanceOf(MentorAlreadyExistException.class)
			.message().isEqualTo("멘토가 이미 존재하는 스쿼드 입니다");
	}

	@Test
	@DisplayName("멘티가 3명인 스쿼드에 멘티로 가입")
	void failJoinSquadWithHasThreeMenteeInSquad() {
		User user1 = createUser("aaa@aaa.com", "nickname1");
		User user2 = createUser("bbb@bbb.com", "nickname2");
		User user3 = createUser("ccc@ccc.com", "nickname3");

		User joinUser = createUser("ddd@ddd.com", "nickname4");

		Category category = createCategory();
		SquadCreateDto createDto = createSquadCreateDto(category.getCategoryName());
		Squad squad = Squad.createSquad(category, createDto);

		UserSquad userSquad1 = UserSquad.createUserSquad(user1, squad, false, true);
		UserSquad userSquad2 = UserSquad.createUserSquad(user2, squad, false, false);
		UserSquad userSquad3 = UserSquad.createUserSquad(user3, squad, false, false);

		LoginUser loginUser = createLoginUser(joinUser.getEmail());
		SquadJoinDto joinRequest = createJoinDto(false);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(joinUser));
		when(userSquadRepository.hasActiveSquadByUserId(joinUser.getId()))
			.thenReturn(false);
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(userSquadRepository.findBySquadId(squad.getId()))
			.thenReturn(Arrays.asList(userSquad1, userSquad2, userSquad3));

		assertThatThrownBy(() -> squadService.joinSquad(joinRequest, squad.getId(), loginUser))
			.isInstanceOf(MentorRequiredException.class)
			.message().isEqualTo("스쿼드 내에 멘토가 필요합니다");
	}

	private SquadCreateDto createSquadCreateDto(String categoryName) {
		return SquadCreateDto.builder()
			.categoryName(categoryName)
			.squadName("squadName")
			.squadExplain("this squad ...")
			.mentor(true)
			.build();
	}

	private Category createCategory() {
		return Category.builder()
			.categoryName("Java")
			.build();
	}

	private LoginUser createLoginUser(String email) {
		return LoginUser.builder()
			.email(email)
			.role(Role.USER)
			.build();
	}

	private User createUser(String email, String nickname) {
		return User.builder()
			.email(email)
			.nickname(nickname)
			.password("password")
			.role(Role.USER)
			.build();
	}

	private SquadJoinDto createJoinDto(boolean isMentor) {
		return SquadJoinDto.builder()
			.mentor(isMentor)
			.build();
	}
}
