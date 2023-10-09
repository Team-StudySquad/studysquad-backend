package com.studysquad.board.service;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.request.BoardEdit;
import com.studysquad.board.request.BoardSearchCondition;
import com.studysquad.board.response.BoardResponse;
import com.studysquad.global.error.exception.NotFoundBoard;
import com.studysquad.global.error.exception.NotFoundProcessMission;
import com.studysquad.global.error.exception.NotFoundSquadBoard;
import com.studysquad.global.error.exception.NotMentorException;
import com.studysquad.global.error.exception.NotThreeSquadBoard;
import com.studysquad.global.error.exception.SquadNotFoundException;
import com.studysquad.global.error.exception.SquadNotProgressException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.mission.domain.Mission;
import com.studysquad.mission.repository.MissionRepository;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

	private final BoardRepository boardRepository;

	private final UserRepository userRepository;

	private final SquadRepository squadRepository;

	private final MissionRepository missionRepository;

	@Transactional
	public void createBoard(BoardCreate boardCreate, Long squadId, LoginUser loginUser) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		if (!squad.getSquadStatus().equals(SquadStatus.PROCESS)) {
			throw new SquadNotProgressException();
		}

		if (!squadRepository.isMentorOfSquad(squad.getId(), user.getId())) {
			throw new NotMentorException();
		}

		Mission processMission = missionRepository.getProcessMissionEntity(squadId)
			.orElseThrow(NotFoundProcessMission::new);

		Long squadBoardCount = missionRepository.hasSquadBoardByMissionId(processMission.getId())
			.orElseThrow(NotFoundSquadBoard::new);

		if (!hasThreeSquadBoard(squadBoardCount)) {
			throw new NotThreeSquadBoard();
		}

		processMission.updateStatusEnd();

		missionRepository.getNextMission(processMission.getMissionSequence())
			.ifPresentOrElse(Mission::updateStatusProcess,
				() -> squad.updateStatus(SquadStatus.END));

		boardRepository.save(Board.builder()
			.user(user)
			.squad(squad)
			.mission(processMission)
			.title(boardCreate.getTitle())
			.content(boardCreate.getContent())
			.build());
	}

	public BoardResponse getBoard(Long boardId) {
		return boardRepository.getBoardById(boardId)
			.orElseThrow(NotFoundBoard::new);
	}

	public Page<BoardResponse> getBoards(BoardSearchCondition searchCondition, Pageable pageable) {
		return boardRepository.getBoards(searchCondition, pageable);
	}

	@Transactional
	public void edit(Long boardId, Long squadId, BoardEdit boardEdit, LoginUser loginUser) {

		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		if (!squadRepository.isMentorOfSquad(squad.getId(), user.getId())) {
			throw new NotMentorException();
		}

		Board board = boardRepository.findById(boardId)
			.orElseThrow(NotFoundBoard::new);

		board.edit(boardEdit.getTitle() != null ? boardEdit.getTitle() : board.getTitle(),
			boardEdit.getContent() != null ? boardEdit.getContent() : board.getContent());

	}

	public void delete(Long boardId, Long squadId, LoginUser loginUser) {

		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		if (!squadRepository.isMentorOfSquad(squad.getId(), user.getId())) {
			throw new NotMentorException();
		}

		Board board = boardRepository.findById(boardId)
			.orElseThrow(NotFoundBoard::new);

		boardRepository.delete(board);
	}

	private boolean hasThreeSquadBoard(Long squadBoardCount) {
		return squadBoardCount.equals(3L);
	}
}
