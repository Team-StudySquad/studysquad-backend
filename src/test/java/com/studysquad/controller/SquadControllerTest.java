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
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.dto.SquadCreateDto;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.squad.service.SquadService;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;
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
	SquadService squadService;
	@Autowired
	ObjectMapper objectMapper;

	@BeforeEach
	void init() {
		userRepository.save(createUser("aaa@aaa.com"));
		categoryRepository.save(createCategory());
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
		SquadCreateDto squadCreateDto = createSquadCreateDto();
		LoginUser loginUser = createLoginUser("aaa@aaa.com");
		squadService.createSquad(squadCreateDto, loginUser);

		SquadCreateDto request = SquadCreateDto.builder()
			.categoryName("Java")
			.squadName("newSquad")
			.squadExplain("newSquadExplain")
			.isMentor(true)
			.build();
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
			.andExpect(jsonPath("$.message").value("이미 활성화된 스쿼드가 존재 합니다"))
			.andDo(print());

		List<Squad> squad = squadRepository.findAll();
		assertThat(squad.get(0).getSquadName()).isNotEqualTo("hello");
		assertThat(squad.get(0).getSquadExplain()).isNotEqualTo("Hello world squad");
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 생성 시 존재하지 않는 카테고리 이름으로 요청 시 오류 응답 바디 리턴")
	void failCreateSquadInvalidCategory() throws Exception {
		SquadCreateDto request = SquadCreateDto.builder()
			.categoryName("non-categoryName")
			.squadName("hello")
			.squadExplain("Hello world squad")
			.isMentor(true)
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

	private LoginUser createLoginUser(String email) {
		return LoginUser.builder()
			.email(email)
			.role(Role.USER)
			.build();
	}

	private SquadCreateDto createSquadCreateDto() {
		return SquadCreateDto.builder()
			.categoryName("Java")
			.squadName("Java squad")
			.squadExplain("study for java")
			.isMentor(true)
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
