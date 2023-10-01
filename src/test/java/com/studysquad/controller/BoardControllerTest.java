package com.studysquad.controller;

import java.util.List;

import org.assertj.core.api.Assertions;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.request.BoardEdit;
import com.studysquad.category.domain.Category;
import com.studysquad.category.repository.CategoryRepository;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.repository.MissionRepository;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.squadboard.repository.SquadBoardRepository;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;
import com.studysquad.usersquad.domain.UserSquad;
import com.studysquad.usersquad.repository.UserSquadRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class BoardControllerTest {
	@Autowired
	MockMvc mockMvc;

	@Autowired
	BoardRepository boardRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	SquadRepository squadRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	MissionRepository missionRepository;

	@Autowired
	SquadBoardRepository squadBoardRepository;

	@Autowired
	UserSquadRepository userSquadRepository;

	@Autowired
	ObjectMapper objectMapper;

	@BeforeEach
	void init() {
		squadBoardRepository.deleteAll();
		userSquadRepository.deleteAll();
		boardRepository.deleteAll();
		missionRepository.deleteAll();
		squadRepository.deleteAll();
		userSquadRepository.deleteAll();
		categoryRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 단건 조회 성공")
	void successGetBoard() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Category category = categoryRepository.save(createCategory("JAVA"));
		Squad squad = squadRepository.save(createSquad(category, "squadName", "squadExplain", SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 1, MissionStatus.PROCESS));

		Board board = boardRepository.save(createBoard(squad, user, mission));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/squad/{squadId}/board/{boardId}", squad.getId(), board.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 단건 조회 성공"))
			.andDo(MockMvcResultHandlers.print());

		List<Board> boards = boardRepository.findAll();
		Assertions.assertThat(boards.get(0)).isNotNull();
		Assertions.assertThat(boards.get(0).getTitle()).isEqualTo("title");
		Assertions.assertThat(boards.get(0).getContent()).isEqualTo("content");
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 생성 성공")
	void successCreateBoard() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Category category = categoryRepository.save(createCategory("JAVA"));
		Squad squad = squadRepository.save(createSquad(category, "squadName", "squadExplain", SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 1, MissionStatus.PROCESS));
		UserSquad userSquad = userSquadRepository.save(createUserSquad(squad, user));

		SquadBoard squadBoard1 = squadBoardRepository.save(createSquadBoard(user, squad, mission));
		SquadBoard squadBoard2 = squadBoardRepository.save(createSquadBoard(user, squad, mission));
		SquadBoard squadBoard3 = squadBoardRepository.save(createSquadBoard(user, squad, mission));

		BoardCreate request = BoardCreate.builder()
			.title("title")
			.content("content")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/squad/{squadId}/board", squad.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.content(json))
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.CREATED.value()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 작성 성공"))
			.andDo(MockMvcResultHandlers.print());

		List<Board> boards = boardRepository.findAll();
		Assertions.assertThat(boards.get(0)).isNotNull();
		Assertions.assertThat(boards.get(0).getTitle()).isEqualTo("title");
		Assertions.assertThat(boards.get(0).getContent()).isEqualTo("content");
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 수정 성공")
	void successUpdateBoard() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Category category = categoryRepository.save(createCategory("JAVA"));
		Squad squad = squadRepository.save(createSquad(category, "squadName", "squadExplain", SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 1, MissionStatus.PROCESS));
		UserSquad userSquad = userSquadRepository.save(createUserSquad(squad, user));

		SquadBoard squadBoard1 = squadBoardRepository.save(createSquadBoard(user, squad, mission));
		SquadBoard squadBoard2 = squadBoardRepository.save(createSquadBoard(user, squad, mission));
		SquadBoard squadBoard3 = squadBoardRepository.save(createSquadBoard(user, squad, mission));

		Board board = boardRepository.save(createBoard(squad, user, mission));

		BoardEdit request = BoardEdit.builder()
			.title("title2")
			.content("content2")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/squad/{squadId}/board/{boardId}", squad.getId(), board.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 수정 성공"))
			.andDo(MockMvcResultHandlers.print());

		List<Board> boards = boardRepository.findAll();
		Assertions.assertThat(boards.get(0)).isNotNull();
		Assertions.assertThat(boards.get(0).getTitle()).isEqualTo("title2");
		Assertions.assertThat(boards.get(0).getContent()).isEqualTo("content2");
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

	private Squad createSquad(Category category, String squadName, String squadExplain, SquadStatus status) {
		return Squad.builder()
			.category(category)
			.squadName(squadName)
			.squadExplain(squadExplain)
			.squadStatus(status)
			.build();
	}

	private Category createCategory(String categoryName) {
		return Category.builder()
			.categoryName(categoryName)
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

	private Board createBoard(Squad squad, User user, Mission mission){
		return Board.builder()
			.squad(squad)
			.user(user)
			.mission(mission)
			.title("title")
			.content("content")
			.build();
	}

	private UserSquad createUserSquad(Squad squad, User user){
		return UserSquad.builder()
			.user(user)
			.squad(squad)
			.isMentor(true)
			.isCreator(true)
			.build();
	}

	private SquadBoard createSquadBoard(User user, Squad squad, Mission mission){
		return SquadBoard.builder()
			.mission(mission)
			.user(user)
			.squad(squad)
			.squadBoardTitle("title")
			.squadBoardContent("content")
			.build();
	}
}
