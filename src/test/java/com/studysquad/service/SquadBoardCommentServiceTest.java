package com.studysquad.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studysquad.global.error.exception.NotFoundSquadBoard;
import com.studysquad.global.error.exception.NotSquadBoardCommentUserException;
import com.studysquad.global.error.exception.NotSquadUserException;
import com.studysquad.global.error.exception.SquadNotFoundException;
import com.studysquad.sqaudboardcomment.domain.SquadBoardComment;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentCreateDto;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentEditDto;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentResponseDto;
import com.studysquad.sqaudboardcomment.repository.SquadBoardCommentRepository;
import com.studysquad.sqaudboardcomment.service.SquadBoardCommentService;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.squadboard.repository.SquadBoardRepository;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class SquadBoardCommentServiceTest {

	@Mock
	SquadRepository squadRepository;
	@Mock
	SquadBoardRepository squadBoardRepository;
	@Mock
	SquadBoardCommentRepository squadBoardCommentRepository;
	@Mock
	UserRepository userRepository;

	@InjectMocks
	SquadBoardCommentService squadBoardCommentService;

	@Test
	@DisplayName("스쿼드 게시글 댓글 전체 조회 성공")
	void successGetSquadBoardComments() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);

		Squad squad = createSquad(SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, squad);

		List<SquadBoardCommentResponseDto> response = IntStream.range(0, 5)
			.mapToObj(i -> SquadBoardCommentResponseDto.builder()
				.commentContent(String.format("commentContent%d", i))
				.build())
			.collect(Collectors.toList());

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(squadBoard.getId()))
			.thenReturn(Optional.of(squadBoard));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(squadBoardCommentRepository.getSquadBoardComments(squadBoard.getId()))
			.thenReturn(response);

		List<SquadBoardCommentResponseDto> result = squadBoardCommentService.getSquadBoardComments(loginUser,
			squad.getId(), squadBoard.getId());

		assertThat(result).hasSize(5);
	}

	@Test
	@DisplayName("존재하지 않는 스쿼드의 스쿼드 게시글 댓글 전체 조회")
	void failGetSquadBoardCommentsNotFoundSquad() {
		Long notFoundSquadId = 100L;

		User user = createUser();
		LoginUser loginUser = createLoginUser(user);

		Squad squad = createSquad(SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, squad);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(notFoundSquadId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(
			() -> squadBoardCommentService.getSquadBoardComments(loginUser, notFoundSquadId, squadBoard.getId()))
			.isInstanceOf(SquadNotFoundException.class)
			.message().isEqualTo("존재하지 않는 스쿼드 입니다");
	}

	@Test
	@DisplayName("존재하지 않는 스쿼드 게시글의 스쿼드 게시글 댓글 전체 조회")
	void failGetSquadBoardCommentNotFoundSquadBoard() {
		Long notFoundSquadBoardId = 100L;

		User user = createUser();
		LoginUser loginUser = createLoginUser(user);

		Squad squad = createSquad(SquadStatus.PROCESS);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(notFoundSquadBoardId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(
			() -> squadBoardCommentService.getSquadBoardComments(loginUser, squad.getId(), notFoundSquadBoardId))
			.isInstanceOf(NotFoundSquadBoard.class)
			.message().isEqualTo("스쿼드 게시글을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("스쿼드에 속하지 않은 사용자가 스쿼드 게시글 댓글 전체 조회")
	void failGetSquadBoardCommentNotUserOfSquad() {

		User user = createUser();
		LoginUser loginUser = createLoginUser(user);

		Squad squad = createSquad(SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, squad);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(squadBoard.getId()))
			.thenReturn(Optional.of(squadBoard));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);

		assertThatThrownBy(
			() -> squadBoardCommentService.getSquadBoardComments(loginUser, squad.getId(), squadBoard.getId()))
			.isInstanceOf(NotSquadUserException.class)
			.message().isEqualTo("스쿼드에 속한 사용자가 아닙니다");
	}

	@Test
	@DisplayName("스쿼드 게시글 댓글 생성 성공")
	void successCreateSquadBoardComment() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);

		Squad squad = createSquad(SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, squad);

		SquadBoardCommentCreateDto createDto = SquadBoardCommentCreateDto.builder()
			.squadBoardCommentContent("squadBoardCommentContent")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(squadBoard.getId()))
			.thenReturn(Optional.of(squadBoard));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);

		squadBoardCommentService.createSquadBoardComment(loginUser, squad.getId(), squadBoard.getId(), createDto);

		verify(squadBoardCommentRepository).save(any(SquadBoardComment.class));
	}

	@Test
	@DisplayName("스쿼드에 속하지 않은 사용자가 스쿼드 게시글 댓글 생성")
	void failCreateSquadBoardCommentNotUserOfSquad() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);

		Squad squad = createSquad(SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, squad);

		SquadBoardCommentCreateDto createDto = SquadBoardCommentCreateDto.builder()
			.squadBoardCommentContent("squadBoardCommentContent")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(squadBoard.getId()))
			.thenReturn(Optional.of(squadBoard));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);

		assertThatThrownBy(
			() -> squadBoardCommentService.createSquadBoardComment(loginUser, squad.getId(), squadBoard.getId(),
				createDto))
			.isInstanceOf(NotSquadUserException.class)
			.message().isEqualTo("스쿼드에 속한 사용자가 아닙니다");

	}

	@Test
	@DisplayName("스쿼드 게시글 댓글 수정 성공")
	void successEditSquadBoardComment() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);

		Squad squad = createSquad(SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, squad);

		SquadBoardComment comment = createSquadBoardComment(user, squadBoard);

		SquadBoardCommentEditDto editDto = SquadBoardCommentEditDto.builder()
			.commentContent("squadBoardCommentEditComment")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(squadBoard.getId()))
			.thenReturn(Optional.of(squadBoard));
		when(squadBoardCommentRepository.findById(comment.getId()))
			.thenReturn(Optional.of(comment));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(squadBoardCommentRepository.isUserOfSquadBoardComment(user.getId(), squadBoard.getId(),
			comment.getId()))
			.thenReturn(true);

		squadBoardCommentService.editSquadBoardComment(loginUser, squad.getId(), squadBoard.getId(),
			comment.getId(), editDto);

		assertThat(comment.getSquadBoardCommentContent()).isEqualTo(editDto.getSquadBoardCommentContent());
	}

	@Test
	@DisplayName("스쿼드에 속하지 않은 사용자가 스쿼드 게시글 댓글 수정")
	void failEditSquadBoardCommentNotUserOfSquad() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);

		Squad squad = createSquad(SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, squad);

		SquadBoardComment comment = createSquadBoardComment(user, squadBoard);

		SquadBoardCommentEditDto editDto = SquadBoardCommentEditDto.builder()
			.commentContent("squadBoardCommentEditComment")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(squadBoard.getId()))
			.thenReturn(Optional.of(squadBoard));
		when(squadBoardCommentRepository.findById(comment.getId()))
			.thenReturn(Optional.of(comment));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(false);

		assertThatThrownBy(
			() -> squadBoardCommentService.editSquadBoardComment(loginUser, squad.getId(), squadBoard.getId(),
				comment.getId(), editDto))
			.isInstanceOf(NotSquadUserException.class)
			.message().isEqualTo("스쿼드에 속한 사용자가 아닙니다");

	}

	@Test
	@DisplayName("댓글 작성자가 아닌 사용자가 스쿼드 게시글 댓글 수정")
	void failEditSquadBoardCommentNotUserOfComment() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);

		Squad squad = createSquad(SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, squad);

		SquadBoardComment comment = createSquadBoardComment(user, squadBoard);

		SquadBoardCommentEditDto editDto = SquadBoardCommentEditDto.builder()
			.commentContent("squadBoardCommentEditComment")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(squadBoard.getId()))
			.thenReturn(Optional.of(squadBoard));
		when(squadBoardCommentRepository.findById(comment.getId()))
			.thenReturn(Optional.of(comment));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(squadBoardCommentRepository.isUserOfSquadBoardComment(user.getId(), squadBoard.getId(), comment.getId()))
			.thenReturn(false);

		assertThatThrownBy(
			() -> squadBoardCommentService.editSquadBoardComment(loginUser, squad.getId(), squadBoard.getId(),
				comment.getId(), editDto))
			.isInstanceOf(NotSquadBoardCommentUserException.class)
			.message().isEqualTo("스쿼드 게시글 댓글을 작성한 사용자가 아닙니다");
	}

	@Test
	@DisplayName("스쿼드 게시글 댓글 삭제 성공")
	void successDeleteSquadBoardComment() {
		User user = createUser();
		LoginUser loginUser = createLoginUser(user);

		Squad squad = createSquad(SquadStatus.PROCESS);

		SquadBoard squadBoard = createSquadBoard(user, squad);

		SquadBoardComment comment = createSquadBoardComment(user, squadBoard);

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadBoardRepository.findById(squadBoard.getId()))
			.thenReturn(Optional.of(squadBoard));
		when(squadBoardCommentRepository.findById(comment.getId()))
			.thenReturn(Optional.of(comment));
		when(squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(squadBoardCommentRepository.isUserOfSquadBoardComment(user.getId(), squadBoard.getId(), comment.getId()))
			.thenReturn(true);

		squadBoardCommentService.deleteSquadBoardComment(loginUser, squad.getId(), squadBoard.getId(), comment.getId());

		verify(squadBoardCommentRepository).deleteById(any());
	}

	private Squad createSquad(SquadStatus status) {
		return Squad.builder()
			.squadName("squadName")
			.squadExplain("squadExplain")
			.squadStatus(status)
			.build();
	}

	private SquadBoard createSquadBoard(User user, Squad squad) {
		return SquadBoard.builder()
			.user(user)
			.squad(squad)
			.squadBoardTitle("squadBoardTitle")
			.squadBoardContent("squadBoardContent")
			.build();
	}

	private SquadBoardComment createSquadBoardComment(User user, SquadBoard squadBoard) {
		return SquadBoardComment.builder()
			.squadBoard(squadBoard)
			.user(user)
			.squadBoardCommentContent("squadBoardCommentContent")
			.build();
	}

	private User createUser() {
		return User.builder()
			.email("aaa@aaa.com")
			.nickname("userA")
			.role(Role.USER)
			.build();
	}

	private LoginUser createLoginUser(User user) {
		return LoginUser.builder()
			.email(user.getEmail())
			.role(user.getRole())
			.build();
	}

}
