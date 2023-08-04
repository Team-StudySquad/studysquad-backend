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
import com.studysquad.global.error.exception.SquadNotProgressException;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.dto.MissionCreateDto;
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
			.missionStatus(MissionStatus.PROGRESS)
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
}
