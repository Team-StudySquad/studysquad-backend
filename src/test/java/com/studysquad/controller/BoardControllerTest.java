package com.studysquad.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
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
import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.request.BoardEdit;
import com.studysquad.board.request.BoardSearchCondition;
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

		mockMvc.perform(get("/api/board/{boardId}", board.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("게시글 단건 조회 성공"))
			.andDo(print());

		Board findBoard = boardRepository.findAll().get(0);
		assertThat(findBoard).isNotNull();
		assertThat(findBoard.getTitle()).isEqualTo(board.getTitle());
		assertThat(findBoard.getContent()).isEqualTo(board.getContent());
	}

	@Test
	@DisplayName("게시글 페이징 조회")
	void successGetBoards() throws Exception {
		User userA = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User userB = userRepository.save(createUser("bbb@bbb.com", "userB"));
		User userC = userRepository.save(createUser("ccc@ccc.com", "userC"));
		User userD = userRepository.save(createUser("ddd@ddd.com", "userD"));

		Category categoryJava = categoryRepository.save(createCategory("JAVA"));
		Category categoryPython = categoryRepository.save(createCategory("Python"));

		Squad squadA = squadRepository.save(
			createSquad(categoryJava, "squadJavaA", "squadJavaExplain", SquadStatus.PROCESS));
		Squad squadB = squadRepository.save(
			createSquad(categoryJava, "squadJavaB", "squadJavaExplain", SquadStatus.PROCESS));
		Squad squadC = squadRepository.save(
			createSquad(categoryJava, "squadJavaC", "squadJavaExplain", SquadStatus.PROCESS));
		Squad squadD = squadRepository.save(
			createSquad(categoryPython, "squadPythonA", "squadPythonExplain", SquadStatus.PROCESS));

		userSquadRepository.saveAll(
			List.of(createMentorUserSquad(squadA, userA), createMentorUserSquad(squadB, userB),
				createMentorUserSquad(squadC, userC),
				createMentorUserSquad(squadD, userD)));

		Mission missionA = missionRepository.save(createMission(squadA, 0, MissionStatus.END));
		Mission missionB = missionRepository.save(createMission(squadB, 0, MissionStatus.END));
		Mission missionC = missionRepository.save(createMission(squadC, 0, MissionStatus.END));
		Mission missionD = missionRepository.save(createMission(squadD, 0, MissionStatus.END));

		boardRepository.saveAll(List.of(createBoard(squadA, userA, missionA), createBoard(squadB, userB, missionB),
			createBoard(squadC, userC, missionC), createBoard(squadD, userD, missionD)));

		BoardSearchCondition cond = BoardSearchCondition.builder()
			.categoryName(categoryJava.getCategoryName())
			.build();

		mockMvc.perform(get("/api/boards")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", "0")
				.param("size", "10")
				.param("categoryName", cond.getCategoryName()))
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("게시글 페이징 조회 성공"))
			.andExpect(jsonPath("$.data.content.length()").value(3))
			.andDo(print());

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 생성 성공")
	void successCreateBoard() throws Exception {
		User userA = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User userB = userRepository.save(createUser("bbb@bbb.com", "userB"));
		User userC = userRepository.save(createUser("ccc@ccc.com", "userC"));
		User userD = userRepository.save(createUser("ddd@ddd.com", "userD"));

		Category category = categoryRepository.save(createCategory("JAVA"));
		Squad squad = squadRepository.save(createSquad(category, "squadName", "squadExplain", SquadStatus.PROCESS));

		Mission mission1 = missionRepository.save(createMission(squad, 1, MissionStatus.PROCESS));
		Mission mission2 = missionRepository.save(createMission(squad, 2, MissionStatus.NOT_PROCESS));

		userSquadRepository.save(createMentorUserSquad(squad, userA));
		userSquadRepository.save(createMenteeUserSquad(squad, userB));
		userSquadRepository.save(createMenteeUserSquad(squad, userC));
		userSquadRepository.save(createMenteeUserSquad(squad, userD));

		squadBoardRepository.saveAll(
			List.of(createSquadBoard(userB, squad, mission1), createSquadBoard(userC, squad, mission1),
				createSquadBoard(userD, squad, mission1)));

		BoardCreate request = BoardCreate.builder()
			.title("title")
			.content("content")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/board", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
			.andExpect(jsonPath("$.message").value("게시글 작성 성공"))
			.andDo(print());

		Board board = boardRepository.findAll().get(0);
		Optional<Mission> processMission = missionRepository.findById(mission2.getId());

		assertThat(board).isNotNull();
		assertThat(processMission).isNotEmpty();
		assertThat(board.getTitle()).isEqualTo(request.getTitle());
		assertThat(board.getContent()).isEqualTo(request.getContent());
		assertThat(processMission.get().getMissionStatus()).isEqualTo(MissionStatus.PROCESS);
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 생성시 스쿼드 게시글이 부족한 경우 오류 응답바디 리턴")
	void failCreateBoardHasNotThreeSquadBoard() throws Exception {
		User userA = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User userB = userRepository.save(createUser("bbb@bbb.com", "userB"));
		User userC = userRepository.save(createUser("ccc@ccc.com", "userC"));
		User userD = userRepository.save(createUser("ddd@ddd.com", "userD"));

		Category category = categoryRepository.save(createCategory("JAVA"));

		Squad squad = squadRepository.save(createSquad(category, "squadJava", "squadJavaExplain", SquadStatus.PROCESS));

		userSquadRepository.save(createMentorUserSquad(squad, userA));
		userSquadRepository.save(createMenteeUserSquad(squad, userB));
		userSquadRepository.save(createMenteeUserSquad(squad, userC));
		userSquadRepository.save(createMenteeUserSquad(squad, userD));

		Mission mission = missionRepository.save(createMission(squad, 0, MissionStatus.PROCESS));

		squadBoardRepository.saveAll(
			List.of(createSquadBoard(userB, squad, mission), createSquadBoard(userC, squad, mission)));

		BoardCreate request = BoardCreate.builder()
			.title("title")
			.content("content")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/board", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시물이 3개가 아닙니다."))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("진행중인 스쿼드가 아닌 경우 오류 응답 바디 리턴")
	void failCreateBoardNotProcessSquad() throws Exception {
		User userA = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Category category = categoryRepository.save(createCategory("JAVA"));
		Squad squad = squadRepository.save(createSquad(category, "endSquad", "endSquadExplain", SquadStatus.END));
		userSquadRepository.save(createMenteeUserSquad(squad, userA));

		BoardCreate request = BoardCreate.builder()
			.title("title")
			.content("content")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/board", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드가 진행중이지 않습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("게시글 수정 성공")
	void successUpdateBoard() throws Exception {
		User userA = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User userB = userRepository.save(createUser("bbb@bbb.com", "userB"));
		User userC = userRepository.save(createUser("ccc@ccc.com", "userC"));
		User userD = userRepository.save(createUser("ddd@ddd,con", "userD"));

		Category category = categoryRepository.save(createCategory("JAVA"));
		Squad squad = squadRepository.save(createSquad(category, "squadName", "squadExplain", SquadStatus.PROCESS));
		Mission mission = missionRepository.save(createMission(squad, 1, MissionStatus.PROCESS));

		userSquadRepository.save(createMentorUserSquad(squad, userA));
		userSquadRepository.save(createMenteeUserSquad(squad, userB));
		userSquadRepository.save(createMenteeUserSquad(squad, userC));
		userSquadRepository.save(createMenteeUserSquad(squad, userD));

		squadBoardRepository.saveAll(
			List.of(createSquadBoard(userB, squad, mission), createSquadBoard(userC, squad, mission),
				createSquadBoard(userD, squad, mission)));

		Board board = boardRepository.save(createBoard(squad, userA, mission));

		BoardEdit request = BoardEdit.builder()
			.title("title2")
			.content("content2")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(
				patch("/api/squad/{squadId}/board/{boardId}", squad.getId(), board.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("게시글 수정 성공"))
			.andDo(print());

		Optional<Board> findBoard = boardRepository.findById(board.getId());
		assertThat(findBoard).isNotEmpty();
		assertThat(findBoard.get().getTitle()).isEqualTo(request.getTitle());
		assertThat(findBoard.get().getContent()).isEqualTo(request.getContent());
	}

	private User createUser(String email, String nickname) {
		return User.builder()
			.email(email)
			.nickname(nickname)
			.role(Role.USER)
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

	private Board createBoard(Squad squad, User user, Mission mission) {
		return Board.builder()
			.squad(squad)
			.user(user)
			.mission(mission)
			.title("title")
			.content("content")
			.build();
	}

	private UserSquad createMentorUserSquad(Squad squad, User user) {
		return UserSquad.builder()
			.user(user)
			.squad(squad)
			.isMentor(true)
			.isCreator(true)
			.build();
	}

	private UserSquad createMenteeUserSquad(Squad squad, User user) {
		return UserSquad.builder()
			.user(user)
			.squad(squad)
			.isMentor(false)
			.isCreator(false)
			.build();
	}

	private SquadBoard createSquadBoard(User user, Squad squad, Mission mission) {
		return SquadBoard.builder()
			.mission(mission)
			.user(user)
			.squad(squad)
			.squadBoardTitle("title")
			.squadBoardContent("content")
			.build();
	}
}
