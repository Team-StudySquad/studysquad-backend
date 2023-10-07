package com.studysquad.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

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
import com.studysquad.mission.repository.MissionRepository;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.squadboard.dto.SquadBoardCreateDto;
import com.studysquad.squadboard.dto.SquadBoardEditDto;
import com.studysquad.squadboard.repository.SquadBoardRepository;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.repository.UserRepository;
import com.studysquad.usersquad.domain.UserSquad;
import com.studysquad.usersquad.repository.UserSquadRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class SquadBoardControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	SquadBoardRepository squadBoardRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	SquadRepository squadRepository;
	@Autowired
	UserSquadRepository userSquadRepository;
	@Autowired
	MissionRepository missionRepository;
	@Autowired
	ObjectMapper objectMapper;

	@BeforeEach
	void init() {
		squadBoardRepository.deleteAll();
		userSquadRepository.deleteAll();
		missionRepository.deleteAll();
		userRepository.deleteAll();
		squadRepository.deleteAll();

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 단건 조회 성공")
	void getSquadBoard() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user, squad, false, false));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));
		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(user, squad, mission, "title", "content"));

		mockMvc.perform(get("/api/squad/{squadId}/squadboard/{squadBoardId}", squad.getId(), squadBoard.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글 단건 조회 성공"))
			.andExpect(jsonPath("$.data.squadBoardId").value(squadBoard.getId()))
			.andExpect(jsonPath("$.data.missionSequence").value(mission.getMissionSequence()))
			.andExpect(jsonPath("$.data.squadBoardTitle").value(squadBoard.getSquadBoardTitle()))
			.andExpect(jsonPath("$.data.squadBoardContent").value(squadBoard.getSquadBoardContent()))
			.andExpect(jsonPath("$.data.creator").value(user.getNickname()))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 단건 조회시 스쿼드가 유효하지 않을 경우 오류 응답 바디 리턴")
	void failGetSquadBoardNotFoundSquad() throws Exception {
		int notFoundSquad = 100;

		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));
		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(user, squad, mission, "title", "content"));

		mockMvc.perform(get("/api/squad/{squadId}/squadboard/{squadBoardId}", notFoundSquad, squadBoard.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("message").value("존재하지 않는 스쿼드 입니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드에 속하지 않은 사용자가 스쿼드 게시글 단건 조회시 오류 응답 바디 리턴")
	void failGetSquadBoardWithUserNotInSquad() throws Exception {
		User userInSquad = userRepository.save(createUser("bbb@bbb.com", "userB"));
		userRepository.save(createUser("aaa@aaa.com", "userA"));

		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(userInSquad, squad, false, false));

		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));
		SquadBoard squadBoard = squadBoardRepository.save(
			createSquadBoard(userInSquad, squad, mission, "title", "content"));

		mockMvc.perform(get("/api/squad/{squadId}/squadboard/{squadBoardId}", squad.getId(), squadBoard.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("message").value("스쿼드에 속한 사용자가 아닙니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 단건 조회시 스쿼드 게시글이 유효하지 않을 경우 오류 응답 바디 리턴")
	void failGetSquadBoardNotFoundSquadBoard() throws Exception {
		int notFoundSquadBoard = 100;

		User user1 = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user1, squad, false, false));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));
		squadBoardRepository.save(createSquadBoard(user1, squad, mission, "title", "content"));

		mockMvc.perform(get("/api/squad/{squadId}/squadboard/{squadBoardId}", squad.getId(), notFoundSquadBoard)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글을 찾을 수 없습니다."))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 전체 조회 성공")
	void getSquadBoards() throws Exception {
		User user1 = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User user2 = userRepository.save(createUser("bbb@bbbb.com", "userB"));
		User user3 = userRepository.save(createUser("ccc@ccc.com", "userC"));

		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));
		Mission mission1 = missionRepository.save(createMission(squad, 0, MissionStatus.END));
		Mission mission2 = missionRepository.save(createMission(squad, 1, MissionStatus.PROCESS));
		Mission mission3 = missionRepository.save(createMission(squad, 2, MissionStatus.NOT_PROCESS));

		userSquadRepository.save(createUserSquad(user1, squad, false, false));
		userSquadRepository.save(createUserSquad(user2, squad, false, false));
		userSquadRepository.save(createUserSquad(user3, squad, false, false));

		squadBoardRepository.save(createSquadBoard(user1, squad, mission1, "title1", "content1"));
		squadBoardRepository.save(createSquadBoard(user2, squad, mission1, "title2", "content2"));
		squadBoardRepository.save(createSquadBoard(user3, squad, mission1, "title3", "content3"));
		squadBoardRepository.save(createSquadBoard(user1, squad, mission2, "title4", "content4"));
		squadBoardRepository.save(createSquadBoard(user2, squad, mission2, "title5", "content5"));

		mockMvc.perform(get("/api/squad/{squadId}/squadboards", squad.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글 전체 조회 성공"))
			.andExpect(jsonPath("$.data.length()").value(5))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 전체 조회시 스쿼드가 유효하지 않을 경우 오류 응답 바디 리턴")
	void failGetSquadBoardsNotFoundSquad() throws Exception {
		int notFoundSquadId = 100;

		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));

		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, false, false));
		squadBoardRepository.save(createSquadBoard(user, squad, mission, "title", "content"));

		mockMvc.perform(get("/api/squad/{squadId}/squadboards", notFoundSquadId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("존재하지 않는 스쿼드 입니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드에 속하지 않은 사용자가 스쿼드 게시글 전체 조회시 오류 응답 바디 리턴")
	void failGetSquadBoardsWithUserNotInSquad() throws Exception {
		userRepository.save(createUser("aaa@aaa.com", "userA"));
		User userInSquad = userRepository.save(createUser("bbb@bbb.com", "userB"));

		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(userInSquad, squad, false, false));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));
		squadBoardRepository.save(createSquadBoard(userInSquad, squad, mission, "title", "content"));

		mockMvc.perform(get("/api/squad/{squadId}/squadboards", squad.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드에 속한 사용자가 아닙니다"))
			.andDo(print());

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 생성")
	void createSquadBoard() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad("squad", "squadExplain", SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user, squad, false, false));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));

		SquadBoardCreateDto request = SquadBoardCreateDto.builder()
			.squadBoardContent("squadBoardContent")
			.squadBoardTitle("squadBoardTitle")
			.build();

		SquadBoard squadBoard = createSquadBoard(user, squad, mission, request.getSquadBoardTitle(),
			request.getSquadBoardContent());

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/squadboard", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글 생성 성공"))
			.andDo(print());

		SquadBoard findSquadBoard = squadBoardRepository.findAll().get(0);
		assertThat(findSquadBoard.getSquadBoardTitle()).isEqualTo(squadBoard.getSquadBoardTitle());
		assertThat(findSquadBoard.getSquadBoardContent()).isEqualTo(squadBoard.getSquadBoardContent());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 생성시 스쿼드가 유효하지 않을 경우 오류 응답 바디 리턴")
	void failCreateSquadBoardNotFoundSquad() throws Exception {
		int notFoundSquadId = 100;

		userRepository.save(createUser("aaa@aaa.com", "userA"));

		SquadBoardCreateDto request = SquadBoardCreateDto.builder()
			.squadBoardTitle("squadBoardTitle")
			.squadBoardContent("squadBoardContent")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/squadboard", notFoundSquadId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("존재하지 않는 스쿼드 입니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글을 멘토가 생성할 경우 오류 응답 바디 리턴")
	void failCreateSquadBoardByMentor() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));

		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user, squad, true, true));

		SquadBoardCreateDto request = SquadBoardCreateDto.builder()
			.squadBoardTitle("squadBoardTitle")
			.squadBoardContent("squadBoardContent")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/squadboard", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("멘티가 아닌 사용자입니다"))
			.andDo(print());

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 생성시 미션이 진행중이 아닐 경우 오류 응답 바디 리턴")
	void failCreateSquadBoardNotProgressMission() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user, squad, false, false));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.NOT_PROCESS));

		SquadBoardCreateDto request = SquadBoardCreateDto.builder()
			.squadBoardTitle("squadBoardTitle")
			.squadBoardContent("squadBoardContent")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/squadboard", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("진행중인 미션을 찾을 수 없습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 생성 시 이미 게시글을 작성한 경우 오류 응답 바디 리턴")
	void failCreateRequestExistSquadBoardByProcessMission() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, false, false));

		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));

		SquadBoardCreateDto request = SquadBoardCreateDto.builder()
			.squadBoardTitle("squadBoard1")
			.squadBoardContent("squadBoardContent1")
			.build();

		squadBoardRepository.save(
			createSquadBoard(user, squad, mission, request.getSquadBoardTitle(), request.getSquadBoardContent()));

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/squadboard", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("이미 게시글을 작성하였습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 수정")
	void editSquadBoard() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));
		SquadBoard squadBoard = squadBoardRepository.save(
			createSquadBoard(user, squad, mission, "squadBoardTitle", "squadBoardContent"));

		SquadBoardEditDto editRequest = SquadBoardEditDto.builder()
			.squadBoardTitle("editSquadBoardTitle")
			.squadBoardContent("editSquadBoardContent")
			.build();

		String json = objectMapper.writeValueAsString(editRequest);

		mockMvc.perform(patch("/api/squad/{squadId}/squadboard/{squadBoardId}", squad.getId(), squadBoard.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글 수정 성공"))
			.andDo(print());

		Optional<SquadBoard> findSquadBoard = squadBoardRepository.findById(squadBoard.getId());
		assertThat(findSquadBoard).isNotEmpty();
		assertThat(findSquadBoard.get().getSquadBoardTitle()).isEqualTo(editRequest.getSquadBoardTitle());
		assertThat(findSquadBoard.get().getSquadBoardContent()).isEqualTo(editRequest.getSquadBoardContent());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 수정시 스쿼드가 유효하지 않을 경우 오류 응답 바디 리턴")
	void failEditSquadBoardNotFoundSquad() throws Exception {
		int notFoundSquad = 100;
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad("squad", "explain", SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));
		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(user, squad, mission, "title", "content"));

		SquadBoardEditDto editRequest = SquadBoardEditDto.builder()
			.squadBoardTitle("editSquadBoard")
			.squadBoardContent("editSquadBoard")
			.build();

		String json = objectMapper.writeValueAsString(editRequest);

		mockMvc.perform(patch("/api/squad/{squadId}/squadboard/{squadBoardId}", notFoundSquad, squadBoard.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("존재하지 않는 스쿼드 입니다"))
			.andDo(print());

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 수정시 스쿼드 게시글이 유효하지 않을 경우 오류 응답 바디 리턴")
	void failEditSquadBoardNotFoundSquadBoard() throws Exception {
		int notFoundSquadBoard = 100;

		userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad("sqaud", "explain", SquadStatus.PROCESS));
		missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));

		SquadBoardEditDto editRequest = SquadBoardEditDto.builder()
			.squadBoardTitle("editSquadBoardTitle")
			.squadBoardContent("editSquadBoardContent")
			.build();

		String json = objectMapper.writeValueAsString(editRequest);

		mockMvc.perform(patch("/api/squad/{squadId}/squadboard/{squadBoardId}", squad.getId(), notFoundSquadBoard)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글을 찾을 수 없습니다."))
			.andDo(print());
	}

	private SquadBoard createSquadBoard(User user, Squad squad, Mission mission, String title, String content) {
		return SquadBoard.builder()
			.user(user)
			.squad(squad)
			.mission(mission)
			.squadBoardTitle(title)
			.squadBoardContent(content)
			.build();
	}

	private User createUser(String email, String nickname) {
		return User.builder()
			.email(email)
			.nickname(nickname)
			.role(Role.USER)
			.build();
	}

	private Squad createSquad(String squadName, String squadExplain, SquadStatus status) {
		return Squad.builder()
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
