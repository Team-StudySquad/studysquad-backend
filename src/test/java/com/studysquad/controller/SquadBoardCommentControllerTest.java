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
import com.studysquad.controller.util.DatabaseCleanUp;
import com.studysquad.sqaudboardcomment.domain.SquadBoardComment;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentCreateDto;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentEditDto;
import com.studysquad.sqaudboardcomment.repository.SquadBoardCommentRepository;
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
public class SquadBoardCommentControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	DatabaseCleanUp databaseCleanUp;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	UserRepository userRepository;
	@Autowired
	SquadRepository squadRepository;
	@Autowired
	UserSquadRepository userSquadRepository;
	@Autowired
	SquadBoardRepository squadBoardRepository;
	@Autowired
	SquadBoardCommentRepository squadBoardCommentRepository;

	@BeforeEach
	void init() {
		databaseCleanUp.cleanUp();
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 전체 조회")
	void getSquadBoardComments() throws Exception {
		User user1 = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User user2 = userRepository.save(createUser("bbb@bbb.com", "userB"));
		User user3 = userRepository.save(createUser("ccc@ccc.com", "userC"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user1, squad, false, false));
		userSquadRepository.save(createUserSquad(user2, squad, false, false));
		userSquadRepository.save(createUserSquad(user3, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user2));

		squadBoardCommentRepository.save(
			createSquadBoardComment(squadBoard, user1, "squadBoardComment1"));
		squadBoardCommentRepository.save(
			createSquadBoardComment(squadBoard, user2, "squadBoardComment2"));
		squadBoardCommentRepository.save(
			createSquadBoardComment(squadBoard, user3, "squadBoardComment3"));

		mockMvc.perform(
				get("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomments", squad.getId(), squadBoard.getId())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글 댓글 전체 조회 성공"))
			.andExpect(jsonPath("$.data.length()").value(3))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드에 속하지 않은 사용자가 스쿼드 게시글 댓글 전체 조회")
	void failGetSquadBoardCommentWithUserNotInSquad() throws Exception {

		userRepository.save(createUser("aaa@aaa.com", "userA"));
		User user2 = userRepository.save(createUser("bbb@bbb.com", "userB"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user2, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user2));

		squadBoardCommentRepository.save(createSquadBoardComment(squadBoard, user2, "squadBoardComment"));

		mockMvc.perform(
				get("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomments", squad.getId(), squadBoard.getId())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드에 속한 사용자가 아닙니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 전체 조회시 스쿼드가 유효하지 않을 경우 오류 응답 바디 리턴")
	void failGetSquadBoardCommentsNotFoundSquad() throws Exception {
		Long notFoundSquadId = 100L;

		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user));

		squadBoardCommentRepository.save(createSquadBoardComment(squadBoard, user, "comment"));

		mockMvc.perform(
				get("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomments", notFoundSquadId,
					squadBoard.getId())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("존재하지 않는 스쿼드 입니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 전체 조회시 스쿼드 게시글이 유효하지 않을 경우 오류 응답 바디 리턴")
	void failGetSquadBoardCommentsNotFoundSquadBoard() throws Exception {
		Long notFoundSquadBoardId = 100L;

		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user));

		squadBoardCommentRepository.save(createSquadBoardComment(squadBoard, user, "comment"));

		mockMvc.perform(get("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomments", squad.getId(),
				notFoundSquadBoardId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글을 찾을 수 없습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 생성 성공")
	void successCreateSquadBoardComments() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user));

		SquadBoardCommentCreateDto request = SquadBoardCommentCreateDto.builder()
			.squadBoardCommentContent("squadBoardCommentContent")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(
				post("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment", squad.getId(), squadBoard.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글 댓글 생성 성공"))
			.andDo(print());

		SquadBoardComment findComment = squadBoardCommentRepository.findAll().get(0);

		assertThat(findComment.getSquadBoardCommentContent()).isEqualTo(request.getSquadBoardCommentContent());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 생성시 스쿼드 게시글이 유효하지 않을 경우 오류 응답 바디 리턴")
	void failCreateSquadBoardCommentsNotFoundSquadBoard() throws Exception {
		Long notFoundSquadBoardId = 100L;

		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, false, false));

		squadBoardRepository.save(createSquadBoard(squad, user));

		SquadBoardCommentCreateDto request = SquadBoardCommentCreateDto.builder()
			.squadBoardCommentContent("squadBoardCommentContent")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment", squad.getId(),
				notFoundSquadBoardId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글을 찾을 수 없습니다"))
			.andDo(print());

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드에 가입하지 않은 사용자가 스쿼드 게시글 댓글 생성시 오류 응답 바디 리턴")
	void failCreateSquadBoardCommentWithNotSquadUser() throws Exception {
		User user1 = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User user2 = userRepository.save(createUser("bbb@bbb.com", "userB"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user2, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user2));

		SquadBoardCommentCreateDto request = SquadBoardCommentCreateDto.builder()
			.squadBoardCommentContent("squadBoardComment")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(
				post("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment", squad.getId(), squadBoard.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드에 속한 사용자가 아닙니다"))
			.andDo(print());

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 수정 성공")
	void successEditSquadBoardComment() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user));

		SquadBoardComment comment = squadBoardCommentRepository.save(
			createSquadBoardComment(squadBoard, user, "squadBoardComment"));

		SquadBoardCommentEditDto request = SquadBoardCommentEditDto.builder()
			.commentContent("editSquadBoardComment")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}",
				squad.getId(), squadBoard.getId(), comment.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글 댓글 수정 성공"))
			.andDo(print());

		Optional<SquadBoardComment> findComment = squadBoardCommentRepository.findById(comment.getId());
		assertThat(findComment).isNotEmpty();
		assertThat(findComment.get().getSquadBoardCommentContent()).isEqualTo(request.getSquadBoardCommentContent());

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 수정시 유효하지 않은 댓글일 경우 오류 응답 바디 리턴")
	void failEditSquadBoardCommentNotFoundSquadBoardComment() throws Exception {
		Long notFoundSquadBoardCommentId = 100L;

		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user));
		squadBoardCommentRepository.save(
			createSquadBoardComment(squadBoard, user, "squadBoardComment"));

		SquadBoardCommentEditDto request = SquadBoardCommentEditDto.builder()
			.commentContent("editSquadBoardComment")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}",
				squad.getId(), squadBoard.getId(), notFoundSquadBoardCommentId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("해당 댓글을 찾을 수 없습니다"))
			.andDo(print());

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드에 속하지 않은 사용자가 스쿼드 게시글 댓글 수정시 오류 응답 바디 리턴")
	void failEditSquadBoardCommentWithNotSquadUser() throws Exception {
		User user1 = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User user2 = userRepository.save(createUser("bbb@bbb.com", "userB"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user2, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user2));
		SquadBoardComment comment = squadBoardCommentRepository.save(
			createSquadBoardComment(squadBoard, user1, "squadBoardComment"));

		SquadBoardCommentEditDto request = SquadBoardCommentEditDto.builder()
			.commentContent("editSquadBoardComment")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}",
				squad.getId(), squadBoard.getId(), comment.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드에 속한 사용자가 아닙니다"))
			.andDo(print());

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 작정자가 아닌 사용자가 댓글 수정시 오류 응답 바디 리턴")
	void failEditSquadBoardCommentWithNotSquadBoardCommentUser() throws Exception {
		User user1 = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User user2 = userRepository.save(createUser("bbb@bbb.com", "userB"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user1, squad, false, false));
		userSquadRepository.save(createUserSquad(user2, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user1));

		SquadBoardComment comment = squadBoardCommentRepository.save(
			createSquadBoardComment(squadBoard, user2, "squadBoardComment"));

		SquadBoardCommentEditDto request = SquadBoardCommentEditDto.builder()
			.commentContent("editSquadBoardComment")
			.build();

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(patch("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}",
				squad.getId(), squadBoard.getId(), comment.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글 댓글을 작성한 사용자가 아닙니다"))
			.andDo(print());

		Optional<SquadBoardComment> findComment = squadBoardCommentRepository.findById(comment.getId());

		assertThat(findComment).isNotEmpty();
		assertThat(findComment.get().getSquadBoardCommentContent()).isEqualTo(comment.getSquadBoardCommentContent());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 삭제 성공")
	void successDeleteSquadBoardComment() throws Exception {
		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));

		userSquadRepository.save(createUserSquad(user, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user));
		SquadBoardComment comment = squadBoardCommentRepository.save(
			createSquadBoardComment(squadBoard, user, "squadBoardComment"));

		mockMvc.perform(delete("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}",
				squad.getId(), squadBoard.getId(), comment.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글 댓글 삭제 성공"))
			.andDo(print());

		Optional<SquadBoardComment> findComment = squadBoardCommentRepository.findById(comment.getId());
		assertThat(findComment).isEmpty();

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 삭제시 유효하지 않은 댓글일 경우 오류 응답 바디 리턴")
	void failDeleteSquadBoardCommentNotFoundSquadBoardComment() throws Exception {
		Long notFoundSquadBoardCommentId = 100L;

		User user = userRepository.save(createUser("aaa@aaa.com", "userA"));
		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user));
		squadBoardCommentRepository.save(createSquadBoardComment(squadBoard, user, "squadBoardComment"));

		mockMvc.perform(delete("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}",
				squad.getId(), squadBoard.getId(), notFoundSquadBoardCommentId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.message").value("해당 댓글을 찾을 수 없습니다"))
			.andDo(print());
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드에 가입하지 않은 사용자가 스쿼드 게시글 댓글 삭제시 오류 응답 바디 리턴")
	void failDeleteSquadBoardCommentWithNotSquadUser() throws Exception {
		User user1 = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User user2 = userRepository.save(createUser("bbb@bbb.com", "userB"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user2, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user2));
		SquadBoardComment comment = squadBoardCommentRepository.save(
			createSquadBoardComment(squadBoard, user1, "squadBoardComment"));

		mockMvc.perform(delete("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}",
				squad.getId(), squadBoard.getId(), comment.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드에 속한 사용자가 아닙니다"))
			.andDo(print());

	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("스쿼드 게시글 댓글 작성자가 아닌 사용자가 댓글 삭제시 오류 응답 바디 리턴")
	void failDeleteSquadBoardCommentWithNotSquadBoardCommentUser() throws Exception {
		User user1 = userRepository.save(createUser("aaa@aaa.com", "userA"));
		User user2 = userRepository.save(createUser("bbb@bbb.com", "userB"));

		Squad squad = squadRepository.save(createSquad(SquadStatus.PROCESS));
		userSquadRepository.save(createUserSquad(user1, squad, false, false));
		userSquadRepository.save(createUserSquad(user2, squad, false, false));

		SquadBoard squadBoard = squadBoardRepository.save(createSquadBoard(squad, user1));
		SquadBoardComment comment = squadBoardCommentRepository.save(
			createSquadBoardComment(squadBoard, user2, "squadBoardComment"));

		mockMvc.perform(delete("/api/squad/{squadId}/squadboard/{squadBoardId}/squadboardcomment/{squadBoardCommentId}",
				squad.getId(), squadBoard.getId(), comment.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("스쿼드 게시글 댓글을 작성한 사용자가 아닙니다"))
			.andDo(print());

		Optional<SquadBoardComment> findComment = squadBoardCommentRepository.findById(comment.getId());

		assertThat(findComment).isNotEmpty();
		assertThat(findComment.get().getSquadBoardCommentContent()).isEqualTo(comment.getSquadBoardCommentContent());

	}

	private User createUser(String email, String userName) {
		return User.builder()
			.email(email)
			.nickname(userName)
			.role(Role.USER)
			.build();
	}

	private Squad createSquad(SquadStatus status) {
		return Squad.builder()
			.squadName("squadName")
			.squadExplain("squadExplain")
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

	private SquadBoard createSquadBoard(Squad squad, User user) {
		return SquadBoard.builder()
			.squad(squad)
			.user(user)
			.squadBoardTitle("squadBoardTitle")
			.squadBoardContent("squadBoardContent")
			.build();
	}

	private SquadBoardComment createSquadBoardComment(SquadBoard squadBoard, User user, String squadBoardComment) {
		return SquadBoardComment.builder()
			.user(user)
			.squadBoard(squadBoard)
			.squadBoardCommentContent(squadBoardComment)
			.build();
	}

}
