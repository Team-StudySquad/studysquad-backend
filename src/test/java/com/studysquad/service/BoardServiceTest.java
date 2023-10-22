package com.studysquad.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.request.BoardEdit;
import com.studysquad.board.request.BoardSearchCondition;
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
			.creator("userA")
			.missionTitle("board")
			.missionContent("boardContent")
			.build();

		when(boardRepository.getBoardById(any(Long.class)))
			.thenReturn(Optional.of(boardResponse));

		BoardResponse result = boardService.getBoard(boardResponse.getBoardId());

		assertThat(result.getBoardId()).isEqualTo(boardResponse.getBoardId());
		assertThat(result.getCreator()).isEqualTo(boardResponse.getCreator());
		assertThat(result.getBoardTitle()).isEqualTo(boardResponse.getBoardTitle());
		assertThat(result.getBoardContent()).isEqualTo(boardResponse.getBoardContent());
	}

	@Test
	@DisplayName("게시물 단건 조회 실패 - 존재하지 않는 게시물")
	void failGetBoard() {

		Long notFoundId = 1L;

		when(boardRepository.getBoardById(any(Long.class)))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> boardService.getBoard(notFoundId))
			.isInstanceOf(NotFoundBoard.class)
			.message()
			.isEqualTo("게시글을 찾을 수 없습니다");
	}

	@Test
	@DisplayName("게시글 페이징 조회")
	void successGetBoards() {
		BoardSearchCondition cond = BoardSearchCondition.builder().build();
		PageRequest page = PageRequest.of(0, 10);
		List<BoardResponse> testData = LongStream.range(1, 31)
			.mapToObj(i -> BoardResponse.builder()
				.boardId(i)
				.creator(String.format("user%d", i))
				.squadName(String.format("squad%d", i))
				.categoryName("JAVA")
				.missionTitle(String.format("mission%d", i))
				.missionContent(String.format("missionContent%d", i))
				.boardTitle(String.format("board%d", i))
				.boardContent(String.format("boardContent%d", i))
				.build())
			.collect(Collectors.toList());

		List<BoardResponse> expectedData = testData.subList(page.getPageNumber(), page.getPageSize());
		Page<BoardResponse> expectedPage = new PageImpl<>(expectedData, page, expectedData.size());

		when(boardRepository.getBoards(cond, page))
			.thenReturn(expectedPage);

		Page<BoardResponse> responseData = boardService.getBoards(cond, page);

		assertThat(responseData.getContent())
			.hasSize(10)
			.isEqualTo(expectedData);
		verify(boardRepository, times(1)).getBoards(eq(cond), eq(page));
	}

	@Test
	@DisplayName("게시글 작성 가능 여부 확인")
	void successIsBoardAllowed() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);
		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squadA", "squadExplain", SquadStatus.PROCESS);
		Mission mission = createMission(squad, 0, MissionStatus.PROCESS);

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

		Boolean result = boardService.isBoardAllowed(squad.getId(), loginUser);

		assertThat(result).isEqualTo(true);
	}

	@Test
	@DisplayName("스쿼드의 게시글 전체 조회")
	void successGetBoardsWithSquad() {
		User user = createUser("aaa@aaa.com", "userA");
		LoginUser loginUser = createLoginUser(user);
		Category category = createCategory("JAVA");
		Squad squad = createSquad(category, "squadA", "squadExplain", SquadStatus.PROCESS);

		List<BoardResponse> boardResponses = LongStream.range(1, 31)
			.mapToObj(i -> BoardResponse.builder()
				.boardId(i)
				.creator(user.getNickname())
				.categoryName(category.getCategoryName())
				.squadName(squad.getSquadName())
				.missionSequence((int)i)
				.missionTitle("missionTitle" + i)
				.missionContent("missionContent" + i)
				.boardTitle("boardTitle" + i)
				.boardContent("boardContent" + i)
				.build())
			.collect(Collectors.toList());

		when(userRepository.findByEmail(loginUser.getEmail()))
			.thenReturn(Optional.of(user));
		when(squadRepository.findById(squad.getId()))
			.thenReturn(Optional.of(squad));
		when(boardRepository.getBoardsWithSquad(squad.getId()))
			.thenReturn(boardResponses);

		List<BoardResponse> response = boardService.getBoardsWithSquad(squad.getId(), loginUser);

		assertThat(response).isNotEmpty();
		assertThat(response).hasSize(boardResponses.size());
		assertThat(response.get(0)).isEqualTo(boardResponses.get(0));
		verify(boardRepository, times(1)).getBoardsWithSquad(squad.getId());
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
	void successBoardUpdate() {
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

		boardService.edit(board.getId(), squad.getId(), request, loginUser);

		AssertionsForClassTypes.assertThat(board.getTitle()).isEqualTo(request.getTitle());
		AssertionsForClassTypes.assertThat(board.getContent()).isEqualTo(request.getContent());
	}

	@Test
	@DisplayName("게시글 수정 성공")
	void successBoardDelete() {
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

	private Board createBoard(Squad squad, User user, Mission mission) {
		return Board.builder()
			.squad(squad)
			.user(user)
			.mission(mission)
			.title("boardTitle")
			.content("boardContent")
			.build();
	}
}
