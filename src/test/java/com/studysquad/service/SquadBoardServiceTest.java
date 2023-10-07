package com.studysquad.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studysquad.category.domain.Category;
import com.studysquad.global.error.exception.ExistSquadBoardByProcessMission;
import com.studysquad.global.error.exception.NotFoundProcessMission;
import com.studysquad.global.error.exception.NotFoundSquadBoard;
import com.studysquad.global.error.exception.NotMenteeException;
import com.studysquad.global.error.exception.NotSquadUserException;
import com.studysquad.global.error.exception.SquadNotFoundException;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.repository.MissionRepository;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.squadboard.dto.SquadBoardCreateDto;
import com.studysquad.squadboard.dto.SquadBoardEditDto;
import com.studysquad.squadboard.dto.SquadBoardResponseDto;
import com.studysquad.squadboard.repository.SquadBoardRepository;
import com.studysquad.squadboard.service.SquadBoardService;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class SquadBoardServiceTest {

	@Mock
	SquadBoardRepository squadBoardRepository;
	@Mock
	UserRepository userRepository;
	@Mock
	SquadRepository squadRepository;
	@Mock
	MissionRepository missionRepository;

	@InjectMocks
	SquadBoardService squadBoardService;

	@Test
	@DisplayName("스쿼드 게시글 단건 조회 성공")
	void successGetSquadBoard() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.RECRUIT);

		SquadBoard squadBoard = createSquadBoard(user, "squadBoardContent", "SquadBoardTitle");

		SquadBoardResponseDto squadBoardDto = SquadBoardResponseDto.builder()
			.squadBoardId(1L)
			.squadBoardTitle("squadBoardTitle")
			.squadBoardContent("squadBoardContent")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(squadBoardRepository.getSquadBoard(squad.getId(), squadBoard.getId()))
			.thenReturn(Optional.of(squadBoardDto));

		squadBoardService.getSquadBoard(loginUser, squad.getId(), squadBoard.getId());

		verify(squadBoardRepository).getSquadBoard(squad.getId(), user.getId());
	}

	@Test
	@DisplayName("존재하지 않은 스쿼드 게시글 단건 조회")
	void failGetSquadBoardNotFoundSquadBoard() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.RECRUIT);

		SquadBoard squadBoard = createSquadBoard(user, "squadBoardContent", "SquadBoardTitle");

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(squadBoardRepository.getSquadBoard(squad.getId(), squadBoard.getId()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadBoardService.getSquadBoard(loginUser, squad.getId(), squadBoard.getId()))
			.isInstanceOf(NotFoundSquadBoard.class)
			.message().isEqualTo("스쿼드 게시글을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("스쿼드에 속하지 않은 사용자가 스쿼드 게시글 단건 조회")
	void failGetSquadBoardNotSquadUser() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.RECRUIT);

		SquadBoard squadBoard = createSquadBoard(user, "squadBoardContent", "SquadBoardTitle");

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);

		assertThatThrownBy(() -> squadBoardService.getSquadBoard(loginUser, squad.getId(), squadBoard.getId()))
			.isInstanceOf(NotSquadUserException.class)
			.message().isEqualTo("스쿼드에 속한 사용자가 아닙니다");

	}

	@Test
	@DisplayName("스쿼드 게시글 전체 조회 성공")
	void successGetSquadBoards() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "explain", SquadStatus.PROCESS);

		List<SquadBoardResponseDto> squadBoardResponse = IntStream.range(0, 5)
			.mapToObj(i -> SquadBoardResponseDto.builder()
				.squadBoardTitle(String.format("title%d", i))
				.squadBoardContent(String.format("content%d", i))
				.build())
			.collect(Collectors.toList());

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(squadBoardRepository.getSquadBoards(squad.getId()))
			.thenReturn(squadBoardResponse);

		List<SquadBoardResponseDto> result = squadBoardService.getSquadBoards(loginUser, squad.getId());

		assertThat(result).hasSize(5);
	}

	@Test
	@DisplayName("존재하지 않는 스쿼드의 스쿼드 게시글 전체 조회")
	void failGetSquadBoardsNotFoundSquad() {
		Long notFoundSquadId = 100L;
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(notFoundSquadId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadBoardService.getSquadBoards(loginUser, notFoundSquadId))
			.isInstanceOf(SquadNotFoundException.class)
			.message().isEqualTo("존재하지 않는 스쿼드 입니다");
	}

	@Test
	@DisplayName("스쿼드에 속하지 않은 사용자가 스쿼드 게시글 전체 조회")
	void failGetSquadBoardsNotSquadUser() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category catagory = createCategory("JAVA");
		Squad squad = createSquad(catagory, "squad", "explain", SquadStatus.PROCESS);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);

		assertThatThrownBy(() -> squadBoardService.getSquadBoards(loginUser, squad.getId()))
			.isInstanceOf(NotSquadUserException.class)
			.message().isEqualTo("스쿼드에 속한 사용자가 아닙니다");

	}

	@Test
	@DisplayName("스쿼드 게시글 생성 성공")
	void successCreateSquadBoard() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.PROCESS);

		Mission mission = Mission.builder()
			.squad(squad)
			.missionTitle("missionTitle")
			.missionContent("missionContent")
			.missionStatus(MissionStatus.PROCESS)
			.missionSequence(0)
			.build();

		SquadBoardCreateDto squadBoardDto = SquadBoardCreateDto.builder()
			.squadBoardContent("squadBoardContent")
			.squadBoardTitle("squadBoardTitle")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);
		when(missionRepository.getProcessMissionEntity(squad.getId()))
			.thenReturn(Optional.of(mission));
		when(squadBoardRepository.hasSquadBoardByProgressMission(user.getId(), mission.getId()))
			.thenReturn(false);

		squadBoardService.createSquadBoard(squadBoardDto, loginUser, squad.getId());

		verify(userRepository).findByEmail(loginUser.getEmail());
		verify(squadRepository).findById(squad.getId());
		verify(squadRepository).isMentorOfSquad(squad.getId(), user.getId());
		verify(missionRepository).getProcessMissionEntity(squad.getId());
		verify(squadBoardRepository).hasSquadBoardByProgressMission(user.getId(), mission.getId());
		verify(squadBoardRepository).save(any(SquadBoard.class));

	}

	@Test
	@DisplayName("스쿼드 게시글 생성시 스쿼드가 유효하지 않음")
	void failCreateSquadBoardSquadNotFound() {
		Long notFoundSquadId = 111L;

		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		SquadBoardCreateDto squadBoardDto = SquadBoardCreateDto.builder()
			.squadBoardContent("squadBoardContent")
			.squadBoardTitle("squadBoardTitle")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(notFoundSquadId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadBoardService.createSquadBoard(squadBoardDto, loginUser, notFoundSquadId))
			.isInstanceOf(SquadNotFoundException.class)
			.message().isEqualTo("존재하지 않는 스쿼드 입니다");

	}

	@Test
	@DisplayName("멘토가 스쿼드 게시글 생성시")
	void failCreateSquadBoardWithMentor() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.PROCESS);

		SquadBoardCreateDto squadBoardDto = SquadBoardCreateDto.builder()
			.squadBoardContent("squadBoardContent")
			.squadBoardTitle("squadBoardTitle")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);

		assertThatThrownBy(() -> squadBoardService.createSquadBoard(squadBoardDto, loginUser, squad.getId()))
			.isInstanceOf(NotMenteeException.class)
			.message().isEqualTo("멘티가 아닌 사용자입니다");

	}

	@Test
	@DisplayName("스쿼드 게시글 생성시 진행중인 미션이 존재하지 않음")
	void failCreateSquadBoardHasNotProgressMission() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.PROCESS);

		SquadBoardCreateDto squadBoardDto = SquadBoardCreateDto.builder()
			.squadBoardContent("squadBoardContent")
			.squadBoardTitle("squadBoardTitle")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);
		when(missionRepository.getProcessMissionEntity(squad.getId()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> squadBoardService.createSquadBoard(squadBoardDto, loginUser, squad.getId()))
			.isInstanceOf(NotFoundProcessMission.class)
			.message().isEqualTo("진행중인 미션을 찾을 수 없습니다");
	}

	@Test
	@DisplayName("스쿼드 게시글 생성 시 진행중인 미션에 대한 게시글이 이미 작성됨")
	void failCreateSquadBoardAlreadyExistSquadBoardByProcessMission() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.PROCESS);

		Mission mission = Mission.builder()
			.squad(squad)
			.missionTitle("missionA")
			.missionContent("missionContent")
			.missionSequence(0)
			.missionStatus(MissionStatus.PROCESS)
			.build();

		SquadBoardCreateDto squadBoardDto = SquadBoardCreateDto.builder()
			.squadBoardContent("squadBoardContent")
			.squadBoardTitle("squadBoardTitle")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);
		when(missionRepository.getProcessMissionEntity(squad.getId()))
			.thenReturn(Optional.ofNullable(mission));
		when(squadBoardRepository.hasSquadBoardByProgressMission(user.getId(), mission.getId()))
			.thenReturn(true);

		assertThatThrownBy(() -> squadBoardService.createSquadBoard(squadBoardDto, loginUser, squad.getId()))
			.isInstanceOf(ExistSquadBoardByProcessMission.class)
			.message().isEqualTo("이미 게시글을 작성하였습니다");
	}

	@Test
	@DisplayName("스쿼드 게시글 수정 성공")
	void successEditSquadBoard() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, "squadBoardContent", "SquadBoardTitle");

		SquadBoardEditDto editDto = SquadBoardEditDto.builder()
			.squadBoardContent("updateSquadBoardContent")
			.squadBoardTitle("updateSquadBoardTitle")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(squadBoard.getId()))
			.thenReturn(Optional.of(squadBoard));
		when(squadBoardRepository.hasSquadBoard(user.getId(), squadBoard.getId(), squad.getId()))
			.thenReturn(true);

		squadBoardService.editSquadBoard(editDto, loginUser, squad.getId(), squadBoard.getId());

		assertThat(squadBoard.getSquadBoardContent()).isEqualTo(editDto.getSquadBoardContent());
		assertThat(squadBoard.getSquadBoardTitle()).isEqualTo(editDto.getSquadBoardTitle());

	}

	@Test
	@DisplayName("스쿼드 게시글 수정시 유효하지 않은 스쿼드")
	void failEditSquadBoardNotFoundSquad() {
		Long notFoundSquadId = 100L;

		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		SquadBoard squadBoard = createSquadBoard(user, "squadBoardContent", "SquadBoardTitle");

		SquadBoardEditDto editDto = SquadBoardEditDto.builder()
			.squadBoardContent("updateSquadBoardContent")
			.squadBoardTitle("updateSquadBoardTitle")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(notFoundSquadId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(
			() -> squadBoardService.editSquadBoard(editDto, loginUser, notFoundSquadId, squadBoard.getId()))
			.isInstanceOf(SquadNotFoundException.class)
			.message().isEqualTo("존재하지 않는 스쿼드 입니다");

	}

	@Test
	@DisplayName("스쿼드 게시글 수정시 유효하지 않은 스쿼드 게시글")
	void failEditSquadBoardNotFoundSquadBoard() {
		Long notFoundSquadBoardId = 100L;

		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "explain", SquadStatus.PROCESS);

		SquadBoardEditDto editDto = SquadBoardEditDto.builder()
			.squadBoardTitle("editSquadBoardTitle")
			.squadBoardContent("editSquadBoardContent")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(notFoundSquadBoardId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(
			() -> squadBoardService.editSquadBoard(editDto, loginUser, squad.getId(), notFoundSquadBoardId))
			.isInstanceOf(NotFoundSquadBoard.class)
			.message().isEqualTo("스쿼드 게시글을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("스쿼드 게시글 수정시 검증되지 않은 스쿼드 게시글")
	void failEditSquadBoardHasNotSquadBoard() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);

		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, "squadBoardContent", "SquadBoardTitle");

		SquadBoardEditDto editDto = SquadBoardEditDto.builder()
			.squadBoardContent("updateSquadBoardContent")
			.squadBoardTitle("updateSquadBoardTitle")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(squadBoard.getId()))
			.thenReturn(Optional.of(squadBoard));
		when(squadBoardRepository.hasSquadBoard(user.getId(), squadBoard.getId(), squad.getId()))
			.thenReturn(false);

		assertThatThrownBy(
			() -> squadBoardService.editSquadBoard(editDto, loginUser, squad.getId(), squadBoard.getId()))
			.isInstanceOf(NotFoundSquadBoard.class)
			.message().isEqualTo("스쿼드 게시글을 찾을 수 없습니다.");
	}

	private Category createCategory(String categoryName) {
		return Category.builder()
			.categoryName(categoryName)
			.build();
	}

	private SquadBoard createSquadBoard(User user, String squadBoardTitle,
		String squadBoardContent) {
		return SquadBoard.builder()
			.user(user)
			.squadBoardTitle(squadBoardTitle)
			.squadBoardContent(squadBoardContent)
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
}