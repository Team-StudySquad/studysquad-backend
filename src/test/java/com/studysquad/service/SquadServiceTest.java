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
import com.studysquad.global.error.exception.NotFoundEndSquad;
import com.studysquad.global.error.exception.NotFoundProcessSquad;
import com.studysquad.global.error.exception.SquadAlreadyFullException;
import com.studysquad.global.error.exception.SquadNotFoundException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.dto.EndSquadDto;
import com.studysquad.squad.dto.ProcessSquadDto;
import com.studysquad.squad.dto.SquadCreateDto;
import com.studysquad.squad.dto.SquadJoinDto;
import com.studysquad.squad.dto.SquadResponseDto;
import com.studysquad.squad.dto.SquadSearchCondition;
import com.studysquad.squad.dto.UserSquadResponseDto;
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
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);
		ProcessSquadDto processSquadDto = ProcessSquadDto.builder()
			.squadId(1L)
			.categoryName("JAVA")
			.squadName("squad")
			.squadExplain("squadExplain")
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
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(user.getEmail()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.getProcessSquad(loginUser))
			.isInstanceOf(UserNotFoundException.class)
			.message().isEqualTo("사용자를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("진행중인 스쿼드가 존재하지 않음")
	void failGetProcessSquadNotFoundProcessSquad() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

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
		SquadSearchCondition cond = SquadSearchCondition.builder().build();
		PageRequest page = PageRequest.of(0, 10);
		List<SquadResponseDto> testData = LongStream.range(1, 31)
			.mapToObj(i -> SquadResponseDto.builder()
				.squadId(i)
				.userCount(3L)
				.squadName(String.format("스쿼드 이름 %d", i))
				.squadExplain(String.format("스쿼드 설명 %d", i))
				.categoryName(String.format("카테고리 이름 %d", i))
				.build())
			.collect(Collectors.toList());

		List<SquadResponseDto> expectedData = testData.subList(page.getPageNumber(), page.getPageSize());
		Page<SquadResponseDto> expectedPage = new PageImpl<>(expectedData, page, expectedData.size());

		when(squadRepository.searchSquadPageByCondition(cond, page))
			.thenReturn(expectedPage);

		Page<SquadResponseDto> responseData = squadService.getRecruitSquads(cond, page);

		assertThat(responseData.getContent())
			.hasSize(10)
			.isEqualTo(expectedData);
		verify(squadRepository, times(1)).searchSquadPageByCondition(eq(cond), eq(page));
	}

	@Test
	@DisplayName("모집중인 스쿼드 단건 조회")
	void successGetRecruitSquad() {
		User user = createUser("aaa@aaa.com", "userA");
		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.RECRUIT);
		SquadResponseDto responseDto = SquadResponseDto.builder()
			.squadId(squad.getId())
			.squadName(squad.getSquadName())
			.squadExplain(squad.getSquadExplain())
			.categoryName(category.getCategoryName())
			.userCount(1L)
			.creatorName(user.getNickname())
			.build();

		when(squadRepository.findSquadBySquadId(squad.getId()))
			.thenReturn(Optional.of(responseDto));
		SquadResponseDto result = squadService.getSquad(squad.getId());

		assertThat(result).isEqualTo(responseDto);
		assertThat(result.getUserCount()).isEqualTo(1L);
		assertThat(result.getSquadId()).isEqualTo(squad.getId());
		assertThat(result.getSquadName()).isEqualTo(squad.getSquadName());
		assertThat(result.getSquadExplain()).isEqualTo(squad.getSquadExplain());
		assertThat(result.getCategoryName()).isEqualTo(category.getCategoryName());
		assertThat(result.getCreatorName()).isEqualTo(user.getNickname());
		verify(squadRepository, times(1)).findSquadBySquadId(squad.getId());
	}

	@Test
	@DisplayName("존재하지 않는 스쿼드 단건 조회")
	void failGetNotFoundSquad() {
		Long notFoundSquadId = 1000L;

		when(squadRepository.findSquadBySquadId(notFoundSquadId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.getSquad(notFoundSquadId))
			.isInstanceOf(SquadNotFoundException.class)
			.message().isEqualTo("존재하지 않는 스쿼드 입니다");
	}

	@Test
	@DisplayName("사용자 스쿼드 조회")
	void successGetUserSquads() {
		User user = createUser("aaa@aaa.com", "userA");
		PageRequest page = PageRequest.of(0, 10);
		LoginUser loginUser = createLoginUser(user);
		List<UserSquadResponseDto> testData = LongStream.range(1, 31)
			.mapToObj(i -> UserSquadResponseDto.builder()
				.squadId(i)
				.squadName(String.format("squad %d", i))
				.squadExplain(String.format("squadExplain %d", i))
				.categoryName("JAVA")
				.squadStatus(SquadStatus.END)
				.build())
			.collect(Collectors.toList());
		List<UserSquadResponseDto> expectedData = testData.subList(page.getPageNumber(), page.getPageSize());
		Page<UserSquadResponseDto> expectedPage = new PageImpl<>(expectedData, page, expectedData.size());

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.getUserSquads(user.getId(), page))
			.thenReturn(expectedPage);

		Page<UserSquadResponseDto> responseData = squadService.getUserSquads(loginUser, page);

		assertThat(responseData.getContent())
			.hasSize(10)
			.isEqualTo(expectedData);
		verify(squadRepository, times(1)).getUserSquads(user.getId(), page);
	}

	@Test
	@DisplayName("종료된 스쿼드 단건 조회")
	void successGetEndSquad() {
		User user = createUser("aaa@aaa.com", "userA");
		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.END);
		LoginUser loginUser = createLoginUser(user);
		EndSquadDto expectedData = EndSquadDto.builder()
			.squadId(squad.getId())
			.squadName(squad.getSquadName())
			.squadExplain(squad.getSquadExplain())
			.categoryName(category.getCategoryName())
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.getEndSquad(squad.getId(), user.getId()))
			.thenReturn(Optional.of(expectedData));

		EndSquadDto result = squadService.getEndSquad(squad.getId(), loginUser);

		assertThat(result.getSquadId()).isEqualTo(expectedData.getSquadId());
		assertThat(result.getSquadName()).isEqualTo(expectedData.getSquadName());
		assertThat(result.getSquadExplain()).isEqualTo(expectedData.getSquadExplain());
		assertThat(result.getCategoryName()).isEqualTo(expectedData.getCategoryName());
		verify(squadRepository, times(1)).getEndSquad(squad.getId(), user.getId());
	}

	@Test
	@DisplayName("해당 스쿼드에 속하지 않은 사용자가 종료된 스쿼드 조회")
	void failGetEndSquadNotInSquad() {
		User user = createUser("aaa@aaa.com", "userA");
		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.END);
		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.getEndSquad(squad.getId(), user.getId()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.getEndSquad(squad.getId(), loginUser))
			.isInstanceOf(NotFoundEndSquad.class)
			.message().isEqualTo("종료된 스쿼드를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("스쿼드 생성")
	void successSquadCreate() {
		User user = createUser("aaa@aaa.com", "userA");
		Category category = createCategory("JAVA");
		LoginUser loginUser = createLoginUser(user);
		SquadCreateDto createRequest = SquadCreateDto.builder()
			.categoryName(category.getCategoryName())
			.squadName("squad")
			.squadExplain("squadExplain")
			.mentor(true)
			.build();

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
		SquadCreateDto createRequest = SquadCreateDto.builder()
			.categoryName("JAVA")
			.squadName("squad")
			.squadExplain("squadExplain")
			.mentor(true)
			.build();
		LoginUser invalidLoginUser = LoginUser.builder()
			.email("invalid@aaa.com")
			.role(Role.USER)
			.build();

		when(userRepository.findByEmail(invalidLoginUser.getEmail()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadService.createSquad(createRequest, invalidLoginUser))
			.isInstanceOf(UserNotFoundException.class)
			.message().isEqualTo("사용자를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("스쿼드 생성 시 이미 활성화된 스쿼드가 존재")
	void failCreateSquadAlreadyExistProgressSquad() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);
		SquadCreateDto createRequest = SquadCreateDto.builder()
			.categoryName("JAVA")
			.squadName("squad")
			.squadExplain("squadExplain")
			.mentor(true)
			.build();

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
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);
		SquadCreateDto createRequest = SquadCreateDto.builder()
			.categoryName("invalidName")
			.squadName("squadName")
			.squadExplain("squadExplain")
			.mentor(true)
			.build();

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
		User user = createUser("aaa@aaa.com", "userA");
		User joinUser = createUser("bbb@bbb.com", "userB");

		Category category = createCategory("JAVA");

		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.RECRUIT);

		UserSquad userSquad = createUserSquad(user, squad, true, true);
		UserSquad joinUserSquad = createUserSquad(joinUser, squad, false, false);

		LoginUser loginUser = createLoginUser(user);

		SquadJoinDto joinRequest = SquadJoinDto.builder()
			.mentor(false)
			.build();

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
		User userA = createUser("aaa@aaa.com", "userA");
		User userB = createUser("bbb@bbb.com", "userB");
		User userC = createUser("ccc@ccc.com", "userC");
		User joinUser = createUser("ddd@ddd.com", "userD");

		Category category = createCategory("JAVA");

		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.RECRUIT);

		UserSquad userSquadA = createUserSquad(userA, squad, true, true);
		UserSquad userSquadB = createUserSquad(userB, squad, false, false);
		UserSquad userSquadC = createUserSquad(userC, squad, false, false);
		UserSquad joinUserSquad = createUserSquad(joinUser, squad, false, false);

		LoginUser loginUser = createLoginUser(joinUser);

		SquadJoinDto joinRequest = SquadJoinDto.builder()
			.mentor(false)
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(joinUser));
		when(userSquadRepository.hasActiveSquadByUserId(joinUser.getId()))
			.thenReturn(false);
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(userSquadRepository.findBySquadId(squad.getId()))
			.thenReturn(Arrays.asList(userSquadA, userSquadB, userSquadC));
		when(userSquadRepository.save(any(UserSquad.class)))
			.thenReturn(joinUserSquad);

		squadService.joinSquad(joinRequest, squad.getId(), loginUser);

		assertThat(squad.getSquadStatus()).isEqualTo(SquadStatus.PROCESS);
		verify(userSquadRepository, times(1)).save(any(UserSquad.class));
	}

	@Test
	@DisplayName("스쿼드 가입 시 활성화 된 스쿼드가 존재")
	void failJoinSquadAlreadyActiveSquad() {
		User user = createUser("aaa@aaa.com", "userA");
		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.RECRUIT);
		LoginUser loginUser = createLoginUser(user);

		SquadJoinDto joinRequest = SquadJoinDto.builder()
			.mentor(false)
			.build();

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
		Long notFoundSquadId = 100L;

		User joinUser = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(joinUser);
		SquadJoinDto joinRequest = SquadJoinDto.builder()
			.mentor(false)
			.build();

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
		User userA = createUser("aaa@aaa.com", "userA");
		User userB = createUser("bbb@bbb.com", "userB");
		User userC = createUser("ccc@ccc.com", "userC");
		User userD = createUser("ddd@ddd.com", "userD");

		User joinUser = createUser("joinUser@aaa.com", "joinUser");

		Category category = createCategory("JAVA");

		Squad squad = createSquad(category, "squadName", "squadExplain", SquadStatus.PROCESS);

		UserSquad userSquadA = createUserSquad(userA, squad, true, true);
		UserSquad userSquadB = createUserSquad(userB, squad, false, false);
		UserSquad userSquadC = createUserSquad(userC, squad, false, false);
		UserSquad userSquadD = createUserSquad(userD, squad, false, false);

		LoginUser loginUser = createLoginUser(joinUser);

		SquadJoinDto joinRequest = SquadJoinDto.builder()
			.mentor(false)
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(joinUser));
		when(userSquadRepository.hasActiveSquadByUserId(joinUser.getId()))
			.thenReturn(false);
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(userSquadRepository.findBySquadId(squad.getId()))
			.thenReturn(Arrays.asList(userSquadA, userSquadB, userSquadC, userSquadD));

		assertThatThrownBy(() -> squadService.joinSquad(joinRequest, squad.getId(), loginUser))
			.isInstanceOf(SquadAlreadyFullException.class)
			.message().isEqualTo("모집이 완료된 스쿼드 입니다");
	}

	@Test
	@DisplayName("멘토가 있는 스쿼드에 멘토로 가입")
	void failJoinSquadMentorAlreadyExist() {
		User user = createUser("aaa@aaa.com", "userA");
		User joinUser = createUser("joinUser@aaa.com", "joinUser");

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.RECRUIT);

		UserSquad userSquad = createUserSquad(user, squad, true, true);

		LoginUser loginUser = createLoginUser(joinUser);

		SquadJoinDto joinRequest = SquadJoinDto.builder()
			.mentor(true)
			.build();

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
		User userA = createUser("aaa@aaa.com", "userA");
		User userB = createUser("bbb@bbb.com", "userB");
		User userC = createUser("ccc@ccc.com", "userC");
		User joinUser = createUser("joinUser@aaa.com", "joinUser");

		Category category = createCategory("JAVA");

		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.RECRUIT);

		UserSquad userSquadA = createUserSquad(userA, squad, false, true);
		UserSquad userSquadB = createUserSquad(userB, squad, false, false);
		UserSquad userSquadC = createUserSquad(userC, squad, false, false);

		LoginUser loginUser = createLoginUser(joinUser);

		SquadJoinDto joinRequest = SquadJoinDto.builder()
			.mentor(false)
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(joinUser));
		when(userSquadRepository.hasActiveSquadByUserId(joinUser.getId()))
			.thenReturn(false);
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(userSquadRepository.findBySquadId(squad.getId()))
			.thenReturn(Arrays.asList(userSquadA, userSquadB, userSquadC));

		assertThatThrownBy(() -> squadService.joinSquad(joinRequest, squad.getId(), loginUser))
			.isInstanceOf(MentorRequiredException.class)
			.message().isEqualTo("스쿼드 내에 멘토가 필요합니다");
	}

	private User createUser(String email, String nickname) {
		return User.builder()
			.email(email)
			.nickname(nickname)
			.role(Role.USER)
			.build();
	}

	private LoginUser createLoginUser(User user) {
		return LoginUser.builder()
			.email(user.getEmail())
			.role(user.getRole())
			.build();
	}

	private Category createCategory(String categoryName) {
		return Category.builder()
			.categoryName(categoryName)
			.build();
	}

	private Squad createSquad(Category category, String squadName, String squadExplain, SquadStatus status) {
		return Squad.builder()
			.category(category)
			.squadName(squadName)
			.squadExplain(squadExplain)
			.squadStatus(status)
			.build();
	}

	private UserSquad createUserSquad(User user, Squad squad, boolean isMentor, boolean isCreator) {
		return UserSquad.builder()
			.user(user)
			.squad(squad)
			.isMentor(isMentor)
			.isCreator(isCreator)
			.build();
	}
}
