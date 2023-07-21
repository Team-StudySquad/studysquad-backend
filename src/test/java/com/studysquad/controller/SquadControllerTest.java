package com.studysquad.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

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
		userRepository.save(createUser("aaa@aaa.com"));
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
		User user = userRepository.save(createUser("bbb@bbb.com"));
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
		User user = userRepository.save(createUser("bbb@bbb.com"));

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
		User user1 = userRepository.save(createUser("bbb@bbb.com"));
		User user2 = userRepository.save(createUser("ccc@ccc.com"));
		User user3 = userRepository.save(createUser("ddd@ddd.com"));
		User user4 = userRepository.save(createUser("eee@eee.com"));

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
		User user = userRepository.save(createUser("bbb@bbb.com"));
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
		User user1 = userRepository.save(createUser("bbb@bbb.com"));
		User user2 = userRepository.save(createUser("ccc@ccc.com"));
		User user3 = userRepository.save(createUser("ddd@ddd.com"));

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
		User user2 = userRepository.save(createUser("bbb@bbb.com"));
		User user3 = userRepository.save(createUser("ccc@ccc.com"));
		User user4 = userRepository.save(createUser("ddd@ddd.com"));

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

	private SquadCreateDto createSquadCreateDto() {
		return SquadCreateDto.builder()
			.categoryName("Java")
			.squadName("Java squad")
			.squadExplain("study for java")
			.mentor(true)
			.build();
	}

	private User createUser(String email) {
		return User.builder()
			.email(email)
			.role(Role.USER)
			.build();
	}

	private Category createCategory() {
		return Category.builder()
			.categoryName("Java")
			.build();
	}
}
