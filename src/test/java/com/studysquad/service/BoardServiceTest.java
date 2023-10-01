package com.studysquad.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.request.BoardEdit;
import com.studysquad.board.response.BoardResponse;
import com.studysquad.board.service.BoardService;
import com.studysquad.category.domain.Category;
import com.studysquad.global.error.exception.NotFoundBoard;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.repository.MissionRepository;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class BoardServiceTest {

	@Mock
	BoardRepository boardRepository;
	@Mock
	SquadRepository squadRepository;
	@Mock
	MissionRepository missionRepository;
	@Mock
	UserRepository userRepository;

	@InjectMocks
	BoardService boardService;

	@Test
	@DisplayName("게시글 단건 조회")
	void successGetBoard() {
		BoardResponse boardResponse = BoardResponse.builder()
			.boardId(1L)
			.nickname("userA")
			.title("board")
			.content("boardContent")
			.build();

		when(boardRepository.getBoardById(any(Long.class)))
			.thenReturn(Optional.of(boardResponse));

		BoardResponse result = boardService.getBoard(boardResponse.getBoardId());

		assertThat(result.getBoardId()).isEqualTo(boardResponse.getBoardId());
		assertThat(result.getNickname()).isEqualTo(boardResponse.getNickname());
		assertThat(result.getTitle()).isEqualTo(boardResponse.getTitle());
		assertThat(result.getContent()).isEqualTo(boardResponse.getContent());
	}

	@Test
	@DisplayName("게시물 단건 조회 실패 - 존재하지 않는 게시물")
	void failGetBoard(){

		Long notFoundId = 1L;

		when(boardRepository.getBoardById(any(Long.class)))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> boardService.getBoard(notFoundId))
			.isInstanceOf(NotFoundBoard.class)
			.message()
			.isEqualTo("게시글을 찾을 수 없습니다");
	}

	@Test
	@DisplayName("게시글 생성 성공")
	void successCreateBoard() {
		User user = createUser("test@test.com", "test");
		LoginUser loginUser = createLoginUser(user);
		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.PROCESS);
		Mission mission = createMission(squad, 0, MissionStatus.PROCESS);
		BoardCreate boardCreate = BoardCreate.builder()
			.title("title")
			.content("content")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(missionRepository.getProcessMissionEntity(squad.getId()))
			.thenReturn(Optional.of(mission));
		when(missionRepository.hasSquadBoardByMissionId(mission.getId()))
			.thenReturn(Optional.of(3L));

		boardService.createBoard(boardCreate, squad.getId(), loginUser);

		verify(userRepository).findByEmail(loginUser.getEmail());
		verify(squadRepository).findById(squad.getId());
		verify(squadRepository).isMentorOfSquad(squad.getId(), user.getId());
		verify(missionRepository).getProcessMissionEntity(squad.getId());
		verify(missionRepository).hasSquadBoardByMissionId(mission.getId());
		verify(boardRepository).save(any(Board.class));

	}

	@Test
	@DisplayName("게시글 수정 성공")
	void successBoardUpdate(){
		User user = createUser("test@test.com", "test");
		LoginUser loginUser = createLoginUser(user);
		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.PROCESS);
		Mission mission = createMission(squad, 0, MissionStatus.PROCESS);
		Board board = createBoard(squad, user, mission);
		BoardEdit request = BoardEdit.builder()
				.title("titleUpdated")
				.content("contentUpdated")
				.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(boardRepository.findById(board.getId()))
			.thenReturn(Optional.of(board));

		boardService.edit(board.getId(), squad.getId(),request, loginUser);

		AssertionsForClassTypes.assertThat(board.getTitle()).isEqualTo(request.getTitle());
		AssertionsForClassTypes.assertThat(board.getContent()).isEqualTo(request.getContent());
	}

	@Test
	@DisplayName("게시글 수정 성공")
	void successBoardDelete(){
		User user = createUser("test@test.com", "test");
		LoginUser loginUser = createLoginUser(user);
		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squad", "squadExplain", SquadStatus.PROCESS);
		Mission mission = createMission(squad, 0, MissionStatus.PROCESS);
		Board board = createBoard(squad, user, mission);
		BoardEdit request = BoardEdit.builder()
			.title("titleUpdated")
			.content("contentUpdated")
			.build();

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(squadRepository.isMentorOfSquad(squad.getId(), user.getId()))
			.thenReturn(true);
		when(boardRepository.findById(board.getId()))
			.thenReturn(Optional.of(board));

		boardService.delete(board.getId(), squad.getId(), loginUser);

		verify(boardRepository, times(1)).delete(board);
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
			.title("boardTitle")
			.content("boardContent")
			.build();
	}
}
