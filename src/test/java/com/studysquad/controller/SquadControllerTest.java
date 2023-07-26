package com.studysquad.controller;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.category.domain.Category;
import com.studysquad.category.repository.CategoryRepository;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.dto.SquadCreateDto;
import com.studysquad.squad.dto.SquadJoinDto;
import com.studysquad.squad.dto.SquadSearchCondition;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.repository.UserRepository;
import com.studysquad.usersquad.domain.UserSquad;
import com.studysquad.usersquad.repository.UserSquadRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class SquadControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	UserRepository userRepository;
	@Autowired
	SquadRepository squadRepository;
	@Autowired
	UserSquadRepository userSquadRepository;
	@Autowired
	CategoryRepository categoryRepository;
	@Autowired
	ObjectMapper objectMapper;

	@BeforeEach
	void init() {
		userRepository.save(createUser("aaa@aaa.com", "nickname"));
	}

	@AfterEach
	void destroy() {
		userSquadRepository.deleteAll();
		squadRepository.deleteAll();
		userRepository.deleteAll();
		categoryRepository.deleteAll();
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 생성")
	void successCreateSquad() throws Exception {
		categoryRepository.save(createCategory());

		SquadCreateDto request = createSquadCreateDto();
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 생성 성공"))
			.andDo(print());

		List<Squad> squad = squadRepository.findAll();
		assertThat(squad.get(0)).isNotNull();
		assertThat(squad.get(0).getSquadName()).isEqualTo("Java squad");
		assertThat(squad.get(0).getSquadExplain()).isEqualTo("study for java");
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 생성 시 활성화된 스쿼드가 존재하는 경우 오류 응답 바디 리턴")
	void failCreateSquadWithExistActiveSquad() throws Exception {
		User user = userRepository.findByEmail("aaa@aaa.com")
			.orElseThrow(UserNotFoundException::new);
		SquadCreateDto createDto = createSquadCreateDto();
		Category category = categoryRepository.save(createCategory());
		Squad squad = squadRepository.save(Squad.createSquad(category, createDto));
		userSquadRepository.save(UserSquad.createUserSquad(user, squad, true, true));

		SquadCreateDto request = SquadCreateDto.builder()
			.categoryName("Java")
			.squadName("newSquad")
			.squadExplain("newSquadExplain")
			.mentor(true)
			.build();
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
			.andExpect(jsonPath("$.message").value("이미 활성화된 스쿼드가 존재 합니다"))
			.andDo(print());

		List<Squad> findSquad = squadRepository.findAll();
		assertThat(findSquad.get(0).getSquadName()).isNotEqualTo("hello");
		assertThat(findSquad.get(0).getSquadExplain()).isNotEqualTo("Hello world squad");
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 생성 시 존재하지 않는 카테고리 이름으로 요청 시 오류 응답 바디 리턴")
	void failCreateSquadInvalidCategory() throws Exception {
		SquadCreateDto request = SquadCreateDto.builder()
			.categoryName("non-categoryName")
			.squadName("hello")
			.squadExplain("Hello world squad")
			.mentor(true)
			.build();
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("유효하지 않은 카테고리 입니다"))
			.andDo(print());

		List<Squad> squad = squadRepository.findAll();
		assertThat(squad).isEmpty();
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 가입 성공")
	void successJoinSquad() throws Exception {
		User user = userRepository.save(createUser("bbb@bbb.com", "nicknameB"));
		SquadCreateDto createDto = createSquadCreateDto();
		Category category = categoryRepository.save(createCategory());
		Squad squad = squadRepository.save(Squad.createSquad(category, createDto));
		userSquadRepository.save(UserSquad.createUserSquad(user, squad, true, true));

		SquadJoinDto request = SquadJoinDto.builder()
			.mentor(false)
			.build();
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/join", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 가입 성공"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("활성화 된 스쿼드가 존재하는 상태로 스쿼드에 가입신청")
	void failJoinSquadWithActiveSquad() throws Exception {
		User userInActiveSquad = userRepository.findByEmail("aaa@aaa.com")
			.orElseThrow(UserNotFoundException::new);
		User user = userRepository.save(createUser("bbb@bbb.com", "nicknameB"));

		SquadCreateDto createDto = createSquadCreateDto();
		Category category = categoryRepository.save(createCategory());
		Squad squad = squadRepository.save(Squad.createSquad(category, createDto));

		userSquadRepository.save(UserSquad.createUserSquad(user, squad, true, true));
		userSquadRepository.save(UserSquad.createUserSquad(userInActiveSquad, squad, false, false));

		SquadJoinDto request = SquadJoinDto.builder()
			.mentor(false)
			.build();
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/join", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
			.andExpect(jsonPath("$.message").value("이미 활성화된 스쿼드가 존재 합니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("존재하지 않는 스쿼드에 가입")
	void failJoinSquadNotFoundSquad() throws Exception {
		int notFoundSquadId = 100;

		SquadJoinDto request = SquadJoinDto.builder()
			.mentor(false)
			.build();
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/join", notFoundSquadId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("존재하지 않는 스쿼드 입니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("모집 완료된 스쿼드에 가입")
	void failJoinSquadAlreadyRecruitEnd() throws Exception {
		User user1 = userRepository.save(createUser("bbb@bbb.com", "nicknameB"));
		User user2 = userRepository.save(createUser("ccc@ccc.com", "nicknameC"));
		User user3 = userRepository.save(createUser("ddd@ddd.com", "nicknameD"));
		User user4 = userRepository.save(createUser("eee@eee.com", "nicknameE"));

		SquadCreateDto createDto = createSquadCreateDto();
		Category category = categoryRepository.save(createCategory());
		Squad squad = squadRepository.save(Squad.createSquad(category, createDto));

		userSquadRepository.save(UserSquad.createUserSquad(user1, squad, true, true));
		userSquadRepository.save(UserSquad.createUserSquad(user2, squad, false, false));
		userSquadRepository.save(UserSquad.createUserSquad(user3, squad, false, false));
		userSquadRepository.save(UserSquad.createUserSquad(user4, squad, false, false));

		SquadJoinDto request = SquadJoinDto.builder()
			.mentor(false)
			.build();
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/join", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("모집이 완료된 스쿼드 입니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("멘토가 존재하는 스쿼드에 멘토로 가입")
	void failJoinAsMentorToSquadWithMentor() throws Exception {
		User user = userRepository.save(createUser("bbb@bbb.com", "nicknameB"));
		SquadCreateDto createDto = createSquadCreateDto();
		Category category = categoryRepository.save(createCategory());
		Squad squad = squadRepository.save(Squad.createSquad(category, createDto));
		userSquadRepository.save(UserSquad.createUserSquad(user, squad, true, true));

		SquadJoinDto request = SquadJoinDto.builder()
			.mentor(true)
			.build();
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/join", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("멘토가 이미 존재하는 스쿼드 입니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("멘티가 3명 존재하는 스쿼드에 멘티로 가입")
	void failJoinSquadHasThreeMenteeInSquad() throws Exception {
		User user1 = userRepository.save(createUser("bbb@bbb.com", "nicknameB"));
		User user2 = userRepository.save(createUser("ccc@ccc.com", "nicknameC"));
		User user3 = userRepository.save(createUser("ddd@ddd.com", "nicknameD"));

		SquadCreateDto createDto = createSquadCreateDto();
		Category category = categoryRepository.save(createCategory());
		Squad squad = squadRepository.save(Squad.createSquad(category, createDto));

		userSquadRepository.save(UserSquad.createUserSquad(user1, squad, false, true));
		userSquadRepository.save(UserSquad.createUserSquad(user2, squad, false, false));
		userSquadRepository.save(UserSquad.createUserSquad(user3, squad, false, false));

		SquadJoinDto request = SquadJoinDto.builder()
			.mentor(false)
			.build();
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/join", squad.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 내에 멘토가 필요합니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("진행중인 스쿼드 조회 성공")
	void successGetProcessSquad() throws Exception {
		User user1 = userRepository.findByEmail("aaa@aaa.com")
			.orElseThrow(UserNotFoundException::new);
		User user2 = userRepository.save(createUser("bbb@bbb.com", "nicknameB"));
		User user3 = userRepository.save(createUser("ccc@ccc.com", "nicknameC"));
		User user4 = userRepository.save(createUser("ddd@ddd.com", "nicknameD"));

		SquadCreateDto createDto = createSquadCreateDto();
		Category category = categoryRepository.save(createCategory());
		Squad squad = Squad.createSquad(category, createDto);
		squad.updateStatus(SquadStatus.PROCESS);

		squadRepository.save(squad);
		userSquadRepository.save(UserSquad.createUserSquad(user1, squad, true, true));
		userSquadRepository.save(UserSquad.createUserSquad(user2, squad, false, false));
		userSquadRepository.save(UserSquad.createUserSquad(user3, squad, false, false));
		userSquadRepository.save(UserSquad.createUserSquad(user4, squad, false, false));

		mockMvc.perform(get("/api/squad/process")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("진행중인 스쿼드 조회 성공"))
			.andExpect(jsonPath("$.data.squadId").value(squad.getId()))
			.andExpect(jsonPath("$.data.categoryName").value(category.getCategoryName()))
			.andExpect(jsonPath("$.data.squadName").value(squad.getSquadName()))
			.andExpect(jsonPath("$.data.squadExplain").value(squad.getSquadExplain()))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("진행중인 스쿼드가 존재하지 않음")
	void failGetProcessSquadNotFoundProcessSquad() throws Exception {
		mockMvc.perform(get("/api/squad/process")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("진행중인 스쿼드를 찾을 수 없습니다"))
			.andDo(print());
	}

	@Test
	@DisplayName("모집중인 스쿼드 조회")
	void successGetRecruitSquads() throws Exception {
		List<User> users = getUsersData();
		Category category = categoryRepository.save(createCategory());
		List<Squad> squads = getSquadsData(category);
		getUserSquadsData(users, squads);

		mockMvc.perform(get("/api/squad/recruit")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("모집중인 스쿼드 조회 성공"))
			.andExpect(jsonPath("$.data.content.length()").value(5))
			.andDo(print());
	}

	@Test
	@DisplayName("멘토를 모집중인 스쿼드 조회")
	void successGetRecruitMentorSquads() throws Exception {
		List<User> users = getUsersData();
		Category category = categoryRepository.save(createCategory());
		List<Squad> squads = getSquadsData(category);
		getUserSquadsData(users, squads);

		SquadSearchCondition cond = SquadSearchCondition.builder()
			.mentor(false)
			.build();

		mockMvc.perform(get("/api/squad/recruit")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", "0")
				.param("size", "10")
				.param("mentor", String.valueOf(cond.getMentor())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("모집중인 스쿼드 조회 성공"))
			.andExpect(jsonPath("$.data.content.length()").value(0))
			.andDo(print());
	}

	@Test
	@DisplayName("멘티를 모집중인 스쿼드 조회")
	void successGetRecruitMenteeSquads() throws Exception {
		List<User> users = getUsersData();
		Category category = categoryRepository.save(createCategory());
		List<Squad> squads = getSquadsData(category);
		getUserSquadsData(users, squads);

		SquadSearchCondition cond = SquadSearchCondition.builder()
			.mentor(true)
			.build();

		mockMvc.perform(get("/api/squad/recruit")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", "0")
				.param("size", "10")
				.param("mentor", String.valueOf(cond.getMentor())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("모집중인 스쿼드 조회 성공"))
			.andExpect(jsonPath("$.data.content.length()").value(5))
			.andDo(print());
	}

	@Test
	@DisplayName("카테고리로 진행중인 스쿼드 조회")
	void successGetRecruitSquadsWithCategory() throws Exception {
		List<User> users = getUsersData();
		Category category = categoryRepository.save(createCategory());
		List<Squad> squads = getSquadsData(category);
		getUserSquadsData(users, squads);

		SquadSearchCondition cond = SquadSearchCondition.builder()
			.categoryName("Java")
			.build();

		mockMvc.perform(get("/api/squad/recruit")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", "0")
				.param("size", "10")
				.param("categoryName", cond.getCategoryName()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("모집중인 스쿼드 조회 성공"))
			.andExpect(jsonPath("$.data.content.length()").value(5))
			.andDo(print());
	}

	@Test
	@DisplayName("멘토와 카테고리로 진행중인 스쿼드 조회")
	void successGetRecruitSquadsWithCategoryAndMentor() throws Exception {
		List<User> users = getUsersData();
		Category category = categoryRepository.save(createCategory());
		List<Squad> squads = getSquadsData(category);
		getUserSquadsData(users, squads);

		SquadSearchCondition cond = SquadSearchCondition.builder()
			.mentor(true)
			.categoryName("Java")
			.build();

		mockMvc.perform(get("/api/squad/recruit")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", "0")
				.param("size", "10")
				.param("categoryName", cond.getCategoryName())
				.param("mentor", String.valueOf(cond.getMentor())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("모집중인 스쿼드 조회 성공"))
			.andExpect(jsonPath("$.data.content.length()").value(5))
			.andDo(print());
	}

	@Test
	@DisplayName("모집중인 스쿼드 단건 조회")
	void successGetRecruitSquad() throws Exception {
		User user1 = userRepository.save(createUser("aaa@aaa.com", "nickname1"));
		User user2 = userRepository.save(createUser("bbb@bbb.com", "nickname2"));
		User user3 = userRepository.save(createUser("ccc@ccc.com", "nickname3"));
		Category category = categoryRepository.save(createCategory());
		Squad squad = squadRepository.save(Squad.builder()
			.squadName("squad")
			.squadExplain("squadExplain")
			.squadState(SquadStatus.RECRUIT)
			.category(category)
			.build());
		userSquadRepository.save(UserSquad.builder()
			.user(user1)
			.squad(squad)
			.isCreator(true)
			.isMentor(true)
			.build());
		userSquadRepository.save(UserSquad.builder()
			.user(user2)
			.squad(squad)
			.isCreator(false)
			.isMentor(false)
			.build());
		userSquadRepository.save(UserSquad.builder()
			.user(user3)
			.squad(squad)
			.isCreator(false)
			.isMentor(false)
			.build());

		mockMvc.perform(get("/api/squad/{squadId}", squad.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 단건 조회 성공"))
			.andExpect(jsonPath("$.data.squadId").value(squad.getId()))
			.andExpect(jsonPath("$.data.userCount").value(3))
			.andExpect(jsonPath("$.data.squadName").value("squad"))
			.andExpect(jsonPath("$.data.squadExplain").value("squadExplain"))
			.andExpect(jsonPath("$.data.creatorName").value(user1.getNickname()))
			.andDo(print());
	}

	@Test
	@DisplayName("존재하지 않는 스쿼드 아이디로 조회")
	void failGetRecruitSquadInvalidSquadId() throws Exception {
		Long notFoundSquadId = 100L;
		mockMvc.perform(get("/api/squad/{squadId}", notFoundSquadId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andDo(print());
	}

	private SquadCreateDto createSquadCreateDto() {
		return SquadCreateDto.builder()
			.categoryName("Java")
			.squadName("Java squad")
			.squadExplain("study for java")
			.mentor(true)
			.build();
	}

	private User createUser(String email, String nickname) {
		return User.builder()
			.email(email)
			.nickname(nickname)
			.role(Role.USER)
			.build();
	}

	private Category createCategory() {
		return Category.builder()
			.categoryName("Java")
			.build();
	}

	private List<UserSquad> getUserSquadsData(List<User> users, List<Squad> squads) {
		return IntStream.range(0, 15)
			.mapToObj(i -> {
				boolean isCreator = i % 3 == 0;
				boolean isMentor = i % 3 == 0;

				User user = users.get(i);
				Squad squad = squads.get(i / 3);

				return UserSquad.createUserSquad(user, squad, isMentor, isCreator);
			})
			.map(userSquadRepository::save)
			.collect(toList());
	}

	private List<Squad> getSquadsData(Category category) {
		return LongStream.range(0, 5)
			.mapToObj(i -> {
				SquadCreateDto createDto = SquadCreateDto.builder()
					.categoryName(category.getCategoryName())
					.squadName("squadName " + i)
					.squadExplain("squadExplain " + i)
					.mentor(true)
					.build();
				return Squad.createSquad(category, createDto);
			})
			.map(squadRepository::save)
			.collect(toList());
	}

	private List<User> getUsersData() {
		return LongStream.range(0, 15)
			.mapToObj(i -> createUser(i + "@aaa.com", "nickname " + i))
			.map(userRepository::save)
			.collect(toList());
	}
}
