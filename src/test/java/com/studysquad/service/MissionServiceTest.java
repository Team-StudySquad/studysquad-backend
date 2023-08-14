package com.studysquad.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studysquad.global.error.exception.NotFoundProcessMission;
import com.studysquad.global.error.exception.NotMentorException;
import com.studysquad.global.error.exception.NotSquadUserException;
import com.studysquad.global.error.exception.ProcessMissionException;
import com.studysquad.global.error.exception.SquadNotProgressException;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.dto.MissionCreateDto;
import com.studysquad.mission.dto.MissionEditDto;
import com.studysquad.mission.dto.MissionResponseDto;
import com.studysquad.mission.repository.MissionRepository;
import com.studysquad.mission.service.MissionService;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class MissionServiceTest {

	@Mock
	MissionRepository missionRepository;
	@Mock
	UserRepository userRepository;
	@Mock
	SquadRepository squadRepository;
	@InjectMocks
	MissionService missionService;

	@Test
	@DisplayName("미션 리스트 조회")
	void successGetMissions() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.PROCESS);
		LoginUser loginUser = createLoginUser(user);

		List<MissionResponseDto> missionResponses = IntStream.range(0, 5)
			.mapToObj(i -> MissionResponseDto.builder()
				.missionTitle(String.format("title%d", i))
				.missionContent(String.format("content%d", i))
				.missionSequence(i)
				.missionStatus(MissionStatus.NOT_PROCESS)
				.build())
			.collect(Collectors.toList());

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(missionRepository.getMissions(squad.getId()))
			.thenReturn(missionResponses);

		List<MissionResponseDto> result = missionService.getMissions(squad.getId(), loginUser);

		assertThat(result).hasSize(5);
	}

	@Test
	@DisplayName("해당 스쿼드 아닌 사용자가 미션 리스트 조회")
	void failGetMissionsWithNotInSquad() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.PROCESS);
		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);

		assertThatThrownBy(() -> missionService.getMissions(squad.getId(), loginUser))
			.isInstanceOf(NotSquadUserException.class)
			.message().isEqualTo("스쿼드에 속한 사용자가 아닙니다");
	}

	@Test
	@DisplayName("진행중인 미션 조회")
	void successGetProcessMission() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.PROCESS);
		MissionResponseDto missionResponseDto = MissionResponseDto.builder()
			.missionId(1L)
			.missionTitle("title")
			.missionContent("content")
			.missionSequence(0)
			.missionStatus(MissionStatus.PROCESS)
			.build();
		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(missionRepository.getProcessMission(squad.getId()))
			.thenReturn(Optional.of(missionResponseDto));

		MissionResponseDto result = missionService.getProcessMission(squad.getId(), loginUser);

		assertThat(result.getMissionId()).isEqualTo(missionResponseDto.getMissionId());
		assertThat(result.getMissionTitle()).isEqualTo(missionResponseDto.getMissionTitle());
		assertThat(result.getMissionContent()).isEqualTo(missionResponseDto.getMissionContent());
		assertThat(result.getMissionSequence()).isEqualTo(missionResponseDto.getMissionSequence());
		assertThat(result.getMissionStatus()).isEqualTo(missionResponseDto.getMissionStatus());
	}

	@Test
	@DisplayName("스쿼드에 속하지 않은 사용자가 진행중인 미션 조회")
	void failGetProcessSquadWithNotInSquad() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.PROCESS);
		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);

		assertThatThrownBy(() -> missionService.getProcessMission(squad.getId(), loginUser))
			.isInstanceOf(NotSquadUserException.class)
			.message().isEqualTo("스쿼드에 속한 사용자가 아닙니다");
	}

	@Test
	@DisplayName("진행중인 미션이 없을 때 진행중인 미션 조회")
	void failGetProcessSquadHasNotProcessMission() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.PROCESS);
		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(missionRepository.getProcessMission(squad.getId()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> missionService.getProcessMission(squad.getId(), loginUser))
			.isInstanceOf(NotFoundProcessMission.class)
			.message().isEqualTo("진행중인 미션을 찾을 수 없습니다");
	}

	@Test
	@DisplayName("미션 생성 성공")
	void successCreateMissions() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.PROCESS);
		Mission mission = createMission(squad, 0, MissionStatus.PROCESS);

		MissionCreateDto missionCreateDto = MissionCreateDto.builder()
			.missionTitle("title")
			.missionContent("content")
			.missionSequence(0)
			.build();

		List<MissionCreateDto> createRequest = Collections.singletonList(missionCreateDto);
		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(missionRepository.saveAll(anyList()))
			.thenReturn(Collections.singletonList(mission));

		missionService.createMission(squad.getId(), createRequest, loginUser);

		verify(missionRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("진행중이지 않은 스쿼드에 미션 생성")
	void failCreateSquadNotProcessSquad() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.RECRUIT);

		MissionCreateDto missionCreateDto = MissionCreateDto.builder()
			.missionTitle("title")
			.missionContent("content")
			.missionSequence(0)
			.build();

		List<MissionCreateDto> createRequest = Collections.singletonList(missionCreateDto);
		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));

		assertThatThrownBy(() -> missionService.createMission(squad.getId(), createRequest, loginUser))
			.isInstanceOf(SquadNotProgressException.class)
			.message().isEqualTo("스쿼드가 진행중이지 않습니다");
	}

	@Test
	@DisplayName("멘토가 아닌 사용자가 미션 생성")
	void failCreateMissionNotMentor() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.PROCESS);

		MissionCreateDto missionCreateDto = MissionCreateDto.builder()
			.missionTitle("title")
			.missionContent("content")
			.missionSequence(0)
			.build();

		List<MissionCreateDto> createRequest = Collections.singletonList(missionCreateDto);
		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);

		assertThatThrownBy(() -> missionService.createMission(squad.getId(), createRequest, loginUser))
			.isInstanceOf(NotMentorException.class)
			.message().isEqualTo("멘토가 아닌 사용자 입니다");
	}

	@Test
	@DisplayName("미션 수정 성공")
	void successEditMission() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.PROCESS);
		Mission mission = createMission(squad, 0, MissionStatus.NOT_PROCESS);

		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();

		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(missionRepository.findById(mission.getId()))
			.thenReturn(Optional.of(mission));

		missionService.editMission(squad.getId(), mission.getId(), editRequest, loginUser);

		AssertionsForClassTypes.assertThat(mission.getMissionTitle()).isEqualTo(editRequest.getMissionTitle());
		AssertionsForClassTypes.assertThat(mission.getMissionContent()).isEqualTo(editRequest.getMissionContent());
	}

	@Test
	@DisplayName("미진행중인 스쿼드에서 미션 수정")
	void failEditMissionWithNotProcessSquad() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.END);
		Mission mission = createMission(squad, 0, MissionStatus.END);

		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();

		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));

		assertThatThrownBy(() -> missionService.editMission(squad.getId(), mission.getId(), editRequest, loginUser))
			.isInstanceOf(SquadNotProgressException.class)
			.message().isEqualTo("스쿼드가 진행중이지 않습니다");
	}

	@Test
	@DisplayName("멘토가 아닌 사용자가 미션 수정")
	void failEditMissionWithNotMentor() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.PROCESS);
		Mission mission = createMission(squad, 0, MissionStatus.PROCESS);

		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();

		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);

		assertThatThrownBy(() -> missionService.editMission(squad.getId(), mission.getId(), editRequest, loginUser))
			.isInstanceOf(NotMentorException.class)
			.message().isEqualTo("멘토가 아닌 사용자 입니다");
	}

	@Test
	@DisplayName("진행중인 미션 수정")
	void failEditMissionWithProcessMission() {
		User user = createUser();
		Squad squad = createSquad(SquadStatus.PROCESS);
		Mission mission = createMission(squad, 0, MissionStatus.PROCESS);

		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();

		LoginUser loginUser = createLoginUser(user);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(missionRepository.findById(mission.getId()))
			.thenReturn(Optional.of(mission));

		assertThatThrownBy(() -> missionService.editMission(squad.getId(), mission.getId(), editRequest, loginUser))
			.isInstanceOf(ProcessMissionException.class)
			.message().isEqualTo("진행중인 미션 입니다");
	}

	private User createUser() {
		return User.builder()
			.email("aaa@aaa.com")
			.nickname("userA")
			.role(Role.USER)
			.build();
	}

	private Squad createSquad(SquadStatus status) {
		return Squad.builder()
			.squadName("squad")
			.squadExplain("squadExplain")
			.squadStatus(status)
			.build();
	}

	private Mission createMission(Squad squad, int sequence, MissionStatus status) {
		return Mission.builder()
			.squad(squad)
			.missionTitle("missionTitle")
			.missionContent("missionContent")
			.missionSequence(sequence)
			.missionStatus(status)
			.build();
	}

	private LoginUser createLoginUser(User user) {
		return LoginUser.builder()
			.email(user.getEmail())
			.role(user.getRole())
			.build();
	}
}
