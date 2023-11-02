package com.studysquad.boardcomment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.boardcomment.domain.BoardComment;
import com.studysquad.boardcomment.dto.BoardCommentCreateDto;
import com.studysquad.boardcomment.dto.BoardCommentEditDto;
import com.studysquad.boardcomment.dto.BoardCommentResponse;
import com.studysquad.boardcomment.repository.BoardCommentRepository;
import com.studysquad.global.error.exception.BoardInfoMismatchException;
import com.studysquad.global.error.exception.NotFoundBoard;
import com.studysquad.global.error.exception.NotFoundBoardComment;
import com.studysquad.global.error.exception.UserInfoMismatchException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardCommentService {

	private final UserRepository userRepository;
	private final BoardRepository boardRepository;
	private final BoardCommentRepository boardCommentRepository;

	public List<BoardCommentResponse> getBoardComments(Long boardId) {
		return boardCommentRepository.getBoardComments(boardId);
	}

	@Transactional
	public void createBoardComment(Long boardId, LoginUser loginUser, BoardCommentCreateDto createRequest) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Board board = boardRepository.findById(boardId)
			.orElseThrow(NotFoundBoard::new);

		boardCommentRepository.save(createRequest.toEntity(user, board));
	}

	@Transactional
	public void editBoardComment(Long boardId, Long boardCommentId, BoardCommentEditDto editRequest,
		LoginUser loginUser) {

		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Board board = boardRepository.findById(boardId)
			.orElseThrow(NotFoundBoard::new);

		BoardComment boardComment = boardCommentRepository.findByIdWithUserAndBoard(boardCommentId)
			.orElseThrow(NotFoundBoardComment::new);

		if (isBoardCommentNotOwnedByBoard(boardComment, board)) {
			throw new BoardInfoMismatchException();
		}
		if (isBoardCommentNotOwnedByUser(boardComment, user)) {
			throw new UserInfoMismatchException();
		}

		boardComment.edit(editRequest);
	}

	private boolean isBoardCommentNotOwnedByBoard(BoardComment boardComment, Board board) {
		return !boardComment.getBoard().getId().equals(board.getId());
	}

	private boolean isBoardCommentNotOwnedByUser(BoardComment boardComment, User user) {
		return !boardComment.getUser().getId().equals(user.getId());
	}
}
