package com.studysquad.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studysquad.global.error.exception.NotMentorException;
import com.studysquad.global.error.exception.ProcessMissionException;
import com.studysquad.global.error.exception.SquadNotProgressException;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.dto.MissionCreateDto;
import com.studysquad.mission.dto.MissionEditDto;
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
	@DisplayName("미션 생성 성공")
	void successCreateMissions() {
		User user = User.builder()
			.email("aaa@aaa.com")
			.role(Role.USER)
			.build();
		Squad squad = Squad.builder()
			.squadName("squad")
			.squadExplain("squadExplain")
			.squadStatus(SquadStatus.PROCESS)
			.build();
		Mission mission = Mission.builder()
			.missionTitle("missionTitle")
			.missionContent("missionContent")
			.missionStatus(MissionStatus.PROCESS)
			.missionSequence(0)
			.build();
		MissionCreateDto missionCreateDto = MissionCreateDto.builder()
			.missionTitle("missionTitle")
			.missionContent("missionContent")
			.missionSequence(0)
			.build();
		List<MissionCreateDto> createRequest = Collections.singletonList(missionCreateDto);
		LoginUser loginUser = LoginUser.builder()
			.email(user.getEmail())
			.role(user.getRole())
			.build();

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
		User user = User.builder()
			.email("aaa@aaa.com")
			.role(Role.USER)
			.build();
		Squad squad = Squad.builder()
			.squadName("squad")
			.squadExplain("squadExplain")
			.squadStatus(SquadStatus.RECRUIT)
			.build();
		MissionCreateDto missionCreateDto = MissionCreateDto.builder()
			.missionTitle("missionTitle")
			.missionContent("missionContent")
			.missionSequence(0)
			.build();
		List<MissionCreateDto> createRequest = Collections.singletonList(missionCreateDto);
		LoginUser loginUser = LoginUser.builder()
			.email(user.getEmail())
			.role(user.getRole())
			.build();

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
		User user = User.builder()
			.email("aaa@aaa.com")
			.role(Role.USER)
			.build();
		Squad squad = Squad.builder()
			.squadName("squad")
			.squadExplain("squadExplain")
			.squadStatus(SquadStatus.PROCESS)
			.build();
		MissionCreateDto missionCreateDto = MissionCreateDto.builder()
			.missionTitle("missionTitle")
			.missionContent("missionContent")
			.missionSequence(0)
			.build();
		List<MissionCreateDto> createRequest = Collections.singletonList(missionCreateDto);
		LoginUser loginUser = LoginUser.builder()
			.email(user.getEmail())
			.role(user.getRole())
			.build();

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
		User user = User.builder()
			.email("aaa@aaa.com")
			.role(Role.USER)
			.build();
		Squad squad = Squad.builder()
			.squadName("squad")
			.squadExplain("squadExplain")
			.squadStatus(SquadStatus.PROCESS)
			.build();
		Mission mission = Mission.builder()
			.squad(squad)
			.missionTitle("missionTitle")
			.missionContent("missionContent")
			.missionStatus(MissionStatus.NOT_PROCESS)
			.missionSequence(0)
			.build();
		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editMissionTitle")
			.missionContent("editMissionContent")
			.build();
		LoginUser loginUser = LoginUser.builder()
			.email(user.getEmail())
			.role(user.getRole())
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(missionRepository.findById(mission.getId()))
			.thenReturn(Optional.of(mission));

		missionService.editMission(squad.getId(), mission.getId(), editRequest, loginUser);

		assertThat(mission.getMissionTitle()).isEqualTo(editRequest.getMissionTitle());
		assertThat(mission.getMissionContent()).isEqualTo(editRequest.getMissionContent());
	}

	@Test
	@DisplayName("미진행중인 스쿼드에서 미션 수정")
	void failEditMissionWithNotProcessSquad() {
		User user = User.builder()
			.email("aaa@aaa.com")
			.role(Role.USER)
			.build();
		Squad squad = Squad.builder()
			.squadName("squad")
			.squadExplain("squadExplain")
			.squadStatus(SquadStatus.END)
			.build();
		Mission mission = Mission.builder()
			.squad(squad)
			.missionTitle("missionTitle")
			.missionContent("missionContent")
			.missionStatus(MissionStatus.END)
			.missionSequence(0)
			.build();
		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();
		LoginUser loginUser = LoginUser.builder()
			.email(user.getEmail())
			.role(user.getRole())
			.build();

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
		User user = User.builder()
			.email("aaa@aaa.com")
			.role(Role.USER)
			.build();
		Squad squad = Squad.builder()
			.squadName("squad")
			.squadExplain("squadExplain")
			.squadStatus(SquadStatus.PROCESS)
			.build();
		Mission mission = Mission.builder().build();
		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();
		LoginUser loginUser = LoginUser.builder()
			.email(user.getEmail())
			.role(user.getRole())
			.build();
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
		User user = User.builder()
			.email("aaa@aaa.com")
			.role(Role.USER)
			.build();
		Squad squad = Squad.builder()
			.squadName("squad")
			.squadExplain("squadExplain")
			.squadStatus(SquadStatus.PROCESS)
			.build();
		Mission mission = Mission.builder()
			.squad(squad)
			.missionTitle("missionTitle")
			.missionContent("missionContent")
			.missionStatus(MissionStatus.PROCESS)
			.missionSequence(0)
			.build();
		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editMissionTitle")
			.missionContent("editMissionContent")
			.build();
		LoginUser loginUser = LoginUser.builder()
			.email(user.getEmail())
			.role(user.getRole())
			.build();

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
}
