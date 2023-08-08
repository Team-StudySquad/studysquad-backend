package com.studysquad.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.dto.MissionCreateDto;
import com.studysquad.mission.dto.MissionEditDto;
import com.studysquad.mission.repository.MissionRepository;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.repository.UserRepository;
import com.studysquad.usersquad.domain.UserSquad;
import com.studysquad.usersquad.repository.UserSquadRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MissionControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	UserRepository userRepository;
	@Autowired
	UserSquadRepository userSquadRepository;
	@Autowired
	SquadRepository squadRepository;
	@Autowired
	MissionRepository missionRepository;

	@BeforeEach
	void init() {
		missionRepository.deleteAll();
		userSquadRepository.deleteAll();
		squadRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("미션 생성 성공")
	void successCreateMission() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, true, true));

		MissionCreateDto createRequest = MissionCreateDto.builder()
			.missionTitle("title")
			.missionContent("content")
			.missionSequence(0)
			.build();

		String json = objectMapper.writeValueAsString(Collections.singletonList(createRequest));

		mockMvc.perform(post("/api/squad/{squadId}/mission", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
			.andExpect(jsonPath("$.message").value("미션 생성 성공"))
			.andDo(print());

		List<Mission> result = missionRepository.findAll();
		assertThat(result).hasSize(1)
			.first()
			.matches(mission -> mission.getMissionStatus().equals(MissionStatus.PROCESS));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("미션 순서가 0인 미션의 상태는 PROGRESS 이다")
	void successCreateMissionWithSequenceZero() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, true, true));

		List<MissionCreateDto> missionCreateDtoList = IntStream.range(0, 5)
			.mapToObj(i -> MissionCreateDto.builder()
				.missionTitle(String.format("title%d", i))
				.missionContent(String.format("content%d", i))
				.missionSequence(i)
				.build())
			.collect(Collectors.toList());

		String json = objectMapper.writeValueAsString(missionCreateDtoList);

		mockMvc.perform(post("/api/squad/{squadId}/mission", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
			.andExpect(jsonPath("$.message").value("미션 생성 성공"))
			.andDo(print());

		List<Mission> missions = missionRepository.findAll();

		assertThat(missions).hasSize(5);
		assertThat(missions)
			.filteredOn(m -> m.getMissionSequence() == 0)
			.extracting(Mission::getMissionStatus)
			.containsOnly(MissionStatus.PROCESS);
		assertThat(missions)
			.filteredOn(m -> m.getMissionSequence() != 0)
			.extracting(Mission::getMissionStatus)
			.containsOnly(MissionStatus.NOT_PROCESS);
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("진행중인 스쿼드만 미션을 생성할 수 있다")
	void failCreateSquadNotProcessSquad() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.RECRUIT));

		userSquadRepository.save(createUserSquad(user, squad, true, true));

		MissionCreateDto createRequest = MissionCreateDto.builder()
			.missionTitle("title")
			.missionContent("content")
			.missionSequence(0)
			.build();

		String json = objectMapper.writeValueAsString(Collections.singletonList(createRequest));

		mockMvc.perform(post("/api/squad/{squadId}/mission", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드가 진행중이지 않습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("미션 생성은 멘토만 할 수 있다")
	void failCreateMissionWithMentee() throws Exception {
		User mentee = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User mentor = userRepository.save(createUser("bbb@bbb.com", "userB"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(mentor, squad, true, true));
		userSquadRepository.save(createUserSquad(mentee, squad, false, false));

		MissionCreateDto createRequest = MissionCreateDto.builder()
			.missionTitle("title")
			.missionContent("content")
			.missionSequence(0)
			.build();

		String json = objectMapper.writeValueAsString(Collections.singletonList(createRequest));

		mockMvc.perform(post("/api/squad/{squadId}/mission", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("멘토가 아닌 사용자 입니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("미션 수정 성공")
	void successEditMission() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.NOT_PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, true, true));

		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();

		String json = objectMapper.writeValueAsString(editRequest);

		mockMvc.perform(patch("/api/squad/{squadId}/mission/{missionId}", squad.getId(), mission.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("미션 수정 성공"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("존재하지 않는 미션")
	void failEditMissionNotFoundMission() throws Exception {
		Long notFoundMissionId = 1L;

		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, true, true));

		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();

		String json = objectMapper.writeValueAsString(editRequest);

		mockMvc.perform(patch("/api/squad/{squadId}/mission/{missionId}", squad.getId(),
				notFoundMissionId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("미션을 찾을 수 없습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("미진행중인 스쿼드에서 미션 수정")
	void failEditMissionNotProcessSquad() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.END));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.NOT_PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, true, true));

		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();

		String json = objectMapper.writeValueAsString(editRequest);

		mockMvc.perform(patch("/api/squad/{squadId}/mission/{missionId}", squad.getId(), mission.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드가 진행중이지 않습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("멘토가 아닌 사용자가 미션 수정")
	void failEditMissionWithNotMentor() throws Exception {
		User mentee = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User mentor = userRepository.save(createUser("bbb@bbb.com", "userB"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.NOT_PROCESS));

		userSquadRepository.save(createUserSquad(mentor, squad, true, true));
		userSquadRepository.save(createUserSquad(mentee, squad, false, false));

		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();

		String json = objectMapper.writeValueAsString(editRequest);

		mockMvc.perform(patch("/api/squad/{squadId}/mission/{missionId}", squad.getId(), mission.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("멘토가 아닌 사용자 입니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("진행중인 미션 수정")
	void failEditMissionWithProcessMission() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, true, true));

		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("editTitle")
			.missionContent("editContent")
			.build();

		String json = objectMapper.writeValueAsString(editRequest);

		mockMvc.perform(patch("/api/squad/{squadId}/mission/{missionId}", squad.getId(), mission.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("진행중인 미션 입니다"))
			.andDo(print());
	}

	private User createUser(String email, String nickname) {
		return User.builder()
			.email(email)
			.nickname(nickname)
			.role(Role.USER)
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
			.missionTitle("title" + sequence)
			.missionContent("content" + sequence)
			.missionSequence(sequence)
			.missionStatus(status)
			.build();
	}
}
