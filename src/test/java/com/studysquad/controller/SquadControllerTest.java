package com.studysquad.controller;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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
		userSquadRepository.deleteAll();
		squadRepository.deleteAll();
		userRepository.deleteAll();
		categoryRepository.deleteAll();
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 생성")
	void successCreateSquad() throws Exception {
		userRepository.save(createUser("aaa@aaa.com", "userA"));
		categoryRepository.save(createCategory("JAVA"));

		SquadCreateDto request = SquadCreateDto.builder()
			.categoryName("JAVA")
			.squadName("squad")
			.squadExplain("squadExplain")
			.mentor(true)
			.build();

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
		assertThat(squad.get(0).getSquadName()).isEqualTo("squad");
		assertThat(squad.get(0).getSquadExplain()).isEqualTo("squadExplain");
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 생성 시 활성화된 스쿼드가 존재하는 경우 오류 응답 바디 리턴")
	void failCreateSquadWithExistActiveSquad() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Category category = categoryRepository.save(createCategory("JAVA"));
		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user, squad, true, true));

		SquadCreateDto request = SquadCreateDto.builder()
			.categoryName(category.getCategoryName())
			.squadName("squad2")
			.squadExplain("squadExplain2")
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
		assertThat(findSquad.get(0).getSquadName()).isNotEqualTo(request.getSquadName());
		assertThat(findSquad.get(0).getSquadExplain()).isNotEqualTo(request.getSquadExplain());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 생성 시 존재하지 않는 카테고리 이름으로 요청 시 오류 응답 바디 리턴")
	void failCreateSquadInvalidCategory() throws Exception {
		userRepository.save(createUser("aaa@aaa.com", "userA"));

		SquadCreateDto request = SquadCreateDto.builder()
			.categoryName("invalidCategoryName")
			.squadName("squad")
			.squadExplain("squadExplain")
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
		userRepository.save(createUser("aaa@aaa.com", "userA"));

		User userB = userRepository.save(createUser("bbb@bbb.com", "userB"));
		Category category = categoryRepository.save(createCategory("JAvA"));
		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.RECRUIT));

		userSquadRepository.save(createUserSquad(userB, squad, true, true));

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
	@DisplayName("모집이 완료된 스쿼드의 상태 변경")
	void successJoinSquadChangeSquadStatus() throws Exception {
		userRepository.save(createUser("aaa@aaa.com", "userA"));

		User userB = userRepository.save(createUser("bbb@bbb.com", "userB"));
		User userC = userRepository.save(createUser("ccc@ccc.com", "userC"));
		User userD = userRepository.save(createUser("ddd@ddd.com", "userD"));

		Category category = categoryRepository.save(createCategory("JAVA"));

		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.RECRUIT));

		userSquadRepository.save(createUserSquad(userB, squad, true, true));
		userSquadRepository.save(createUserSquad(userC, squad, false, false));
		userSquadRepository.save(createUserSquad(userD, squad, false, false));

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

		Optional<Squad> findSquad = squadRepository.findById(squad.getId());
		assertThat(findSquad).isNotEmpty();
		assertThat(findSquad.get().getSquadStatus()).isEqualTo(SquadStatus.PROCESS);
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("활성화 된 스쿼드가 존재하는 상태로 스쿼드에 가입신청")
	void failJoinSquadWithActiveSquad() throws Exception {
		User userWithActiveSquad = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User user = userRepository.save(createUser("bbb@bbb.com", "userB"));

		Category category = categoryRepository.save(createCategory("JAVA"));

		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.RECRUIT));

		userSquadRepository.save(createUserSquad(user, squad, true, true));
		userSquadRepository.save(createUserSquad(userWithActiveSquad, squad, false, false));

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

		userRepository.save(createUser("aaa@aaa.com", "userA"));

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
		User userA = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User userB = userRepository.save(createUser("bbb@bbb.com", "userB"));
		User userC = userRepository.save(createUser("ccc@ccc.com", "userC"));
		User userD = userRepository.save(createUser("ddd@ddd.com", "userD"));

		Category category = categoryRepository.save(createCategory("JAVA"));

		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.END));

		userSquadRepository.save(createUserSquad(userA, squad, true, true));
		userSquadRepository.save(createUserSquad(userB, squad, false, false));
		userSquadRepository.save(createUserSquad(userC, squad, false, false));
		userSquadRepository.save(createUserSquad(userD, squad, false, false));

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
		userRepository.save(createUser("aaa@aaa.com", "userA"));

		User user = userRepository.save(createUser("bbb@bbb.com", "userB"));

		Category category = categoryRepository.save(createCategory("JAVA"));

		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.RECRUIT));

		userSquadRepository.save(createUserSquad(user, squad, true, true));

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
		userRepository.save(createUser("aaa@aaa.com", "userA"));

		User userB = userRepository.save(createUser("bbb@bbb.com", "userB"));
		User userC = userRepository.save(createUser("ccc@ccc.com", "userC"));
		User userD = userRepository.save(createUser("ddd@ddd.com", "userD"));

		Category category = categoryRepository.save(createCategory("JAVA"));

		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.RECRUIT));

		userSquadRepository.save(createUserSquad(userB, squad, false, true));
		userSquadRepository.save(createUserSquad(userC, squad, false, false));
		userSquadRepository.save(createUserSquad(userD, squad, false, false));

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
		User userA = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User userB = userRepository.save(createUser("bbb@bbb.com", "userB"));
		User userC = userRepository.save(createUser("ccc@ccc.com", "userC"));
		User userD = userRepository.save(createUser("ddd@ddd.com", "userD"));

		Category category = categoryRepository.save(createCategory("JAVA"));

		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(userA, squad, true, true));
		userSquadRepository.save(createUserSquad(userB, squad, false, false));
		userSquadRepository.save(createUserSquad(userC, squad, false, false));
		userSquadRepository.save(createUserSquad(userD, squad, false, false));

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
		userRepository.save(createUser("aaa@aaa.com", "userA"));

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
		Category category = categoryRepository.save(createCategory("JAVA"));
		List<User> users = createUsersData();
		List<Squad> squads = createSquadsData(category);
		createUserSquadsData(users, squads);

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
		Category category = categoryRepository.save(createCategory("JAVA"));
		List<User> users = createUsersData();
		List<Squad> squads = createSquadsData(category);
		createUserSquadsData(users, squads);

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
		Category category = categoryRepository.save(createCategory("JAVA"));
		List<User> users = createUsersData();
		List<Squad> squads = createSquadsData(category);
		createUserSquadsData(users, squads);

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
		Category category = categoryRepository.save(createCategory("JAVA"));
		List<User> users = createUsersData();
		List<Squad> squads = createSquadsData(category);
		createUserSquadsData(users, squads);

		SquadSearchCondition cond = SquadSearchCondition.builder()
			.categoryName(category.getCategoryName())
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
		Category category = categoryRepository.save(createCategory("JAVA"));
		List<User> users = createUsersData();
		List<Squad> squads = createSquadsData(category);
		createUserSquadsData(users, squads);

		SquadSearchCondition cond = SquadSearchCondition.builder()
			.mentor(true)
			.categoryName(category.getCategoryName())
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
		User userA = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User userB = userRepository.save(createUser("bbb@bbb.com", "userB"));
		User userC = userRepository.save(createUser("ccc@ccc.com", "userC"));

		Category category = categoryRepository.save(createCategory("JAVA"));

		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.RECRUIT));

		userSquadRepository.save(createUserSquad(userA, squad, true, true));
		userSquadRepository.save(createUserSquad(userB, squad, false, false));
		userSquadRepository.save(createUserSquad(userC, squad, false, false));

		mockMvc.perform(get("/api/squad/{squadId}", squad.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 단건 조회 성공"))
			.andExpect(jsonPath("$.data.squadId").value(squad.getId()))
			.andExpect(jsonPath("$.data.userCount").value(3))
			.andExpect(jsonPath("$.data.squadName").value(squad.getSquadName()))
			.andExpect(jsonPath("$.data.squadExplain").value(squad.getSquadExplain()))
			.andExpect(jsonPath("$.data.creatorName").value(userA.getNickname()))
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

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("사용자 스쿼드 조회")
	void successGetUserSquads() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));

		Category category = categoryRepository.save(createCategory("JAVA"));

		Squad squad1 = squadRepository.save(createSquad(category,
			"squad1", "squadExplain1", SquadStatus.END));
		Squad squad2 = squadRepository.save(createSquad(category,
			"squad2", "squadExplain2", SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad1, true, true));
		userSquadRepository.save(createUserSquad(user, squad2, true, true));

		mockMvc.perform(get("/api/squads")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("사용자 스쿼드 조회 성공"))
			.andExpect(jsonPath("$.data.content.length()").value(2))
			.andExpect(jsonPath("$.data.content[0].squadId").value(squad2.getId()))
			.andExpect(jsonPath("$.data.content[0].squadName").value(squad2.getSquadName()))
			.andExpect(jsonPath("$.data.content[0].squadExplain").value(squad2.getSquadExplain()))
			.andExpect(jsonPath("$.data.content[0].squadStatus").value(squad2.getSquadStatus().toString()))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("사용자 스쿼드가 없는 상태로 조회")
	void successGetUserSquadEmptyUserSquad() throws Exception {
		userRepository.save(createUser("aaa@aaa.com", "userA"));

		mockMvc.perform(get("/api/squads")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("사용자 스쿼드 조회 성공"))
			.andExpect(jsonPath("$.data.content.length()").value(0))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("종료된 스쿼드 단건 조회 성공")
	void successGetEndSquad() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Category category = categoryRepository.save(createCategory("JAVA"));
		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.END));
		userSquadRepository.save(createUserSquad(user, squad, true, true));

		mockMvc.perform(get("/api/squad/end/{squadId}", squad.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("종료된 스쿼드 단건 조회 성공"))
			.andExpect(jsonPath("$.data.squadId").value(squad.getId()))
			.andExpect(jsonPath("$.data.squadName").value(squad.getSquadName()))
			.andExpect(jsonPath("$.data.squadExplain").value(squad.getSquadExplain()))
			.andExpect(jsonPath("$.data.categoryName").value(squad.getCategory().getCategoryName()))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("종료된 스쿼드에 속하지 않은 사용자가 요청")
	void failGetEndSquadNotInSquad() throws Exception {
		userRepository.save(createUser("aaa@aaa.com", "userA"));

		User user = userRepository.save(createUser("bbb@bbb.com", "userB"));
		Category category = categoryRepository.save(createCategory("JAVA"));
		Squad squad = squadRepository.save(createSquad(category,
			"squad", "squadExplain", SquadStatus.END));
		userSquadRepository.save(createUserSquad(user, squad, true, true));

		mockMvc.perform(get("/api/squad/end/{squadId}", squad.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("종료된 스쿼드를 찾을 수 없습니다"))
			.andDo(print());
	}

	private User createUser(String email, String nickname) {
		return User.builder()
			.email(email)
			.nickname(nickname)
			.role(Role.USER)
			.build();
	}

	private Category createCategory(String categoryName) {
		return Category.builder()
			.categoryName(categoryName)
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

	private UserSquad createUserSquad(User user, Squad squad, boolean isMentor, boolean isCreator) {
		return UserSquad.builder()
			.user(user)
			.squad(squad)
			.isMentor(isMentor)
			.isCreator(isCreator)
			.build();
	}

	private void createUserSquadsData(List<User> users, List<Squad> squads) {
		IntStream.range(0, 15)
			.mapToObj(i -> {
				boolean isCreator = i % 3 == 0;
				boolean isMentor = i % 3 == 0;

				User user = users.get(i);
				Squad squad = squads.get(i / 3);

				return UserSquad.builder()
					.user(user)
					.squad(squad)
					.isMentor(isMentor)
					.isCreator(isCreator)
					.build();
			})
			.forEach(userSquadRepository::save);
	}

	private List<Squad> createSquadsData(Category category) {
		return LongStream.range(0, 5)
			.mapToObj(i -> Squad.builder()
				.category(category)
				.squadName(String.format("squad %d", i))
				.squadExplain(String.format("squadExplain %d", i))
				.squadStatus(SquadStatus.RECRUIT)
				.build())
			.map(squadRepository::save)
			.collect(toList());
	}

	private List<User> createUsersData() {
		return LongStream.range(0, 15)
			.mapToObj(i -> User.builder()
				.email(String.format("%d@aaa.com", i))
				.nickname(String.format("user%d", i))
				.build())
			.map(userRepository::save)
			.collect(toList());
	}
}
