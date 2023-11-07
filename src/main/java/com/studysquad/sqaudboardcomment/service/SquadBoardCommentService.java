package com.studysquad.sqaudboardcomment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studysquad.global.error.exception.NotFoundSquadBoard;
import com.studysquad.global.error.exception.NotFoundSquadBoardCommentException;
import com.studysquad.global.error.exception.NotSquadBoardCommentUserException;
import com.studysquad.global.error.exception.NotSquadUserException;
import com.studysquad.global.error.exception.SquadNotFoundException;
import com.studysquad.global.error.exception.UserNotFoundException;
import com.studysquad.sqaudboardcomment.domain.SquadBoardComment;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentCreateDto;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentEditDto;
import com.studysquad.sqaudboardcomment.dto.SquadBoardCommentResponseDto;
import com.studysquad.sqaudboardcomment.repository.SquadBoardCommentRepository;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squad.repository.SquadRepository;
import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.squadboard.repository.SquadBoardRepository;
import com.studysquad.user.domain.User;
import com.studysquad.user.dto.LoginUser;
import com.studysquad.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SquadBoardCommentService {

	private final SquadBoardRepository squadBoardRepository;
	private final UserRepository userRepository;
	private final SquadRepository squadRepository;
	private final SquadBoardCommentRepository squadBoardCommentRepository;

	public List<SquadBoardCommentResponseDto> getSquadBoardComments(LoginUser loginUser, Long squadId,
		Long squadBoardId) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		SquadBoard squadBoard = squadBoardRepository.findById(squadBoardId)
			.orElseThrow(NotFoundSquadBoard::new);

		if (!squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			throw new NotSquadUserException();

		return squadBoardCommentRepository.getSquadBoardComments(squadBoard.getId());
	}

	@Transactional
	public void createSquadBoardComment(LoginUser loginUser, Long squadId, Long squadBoardId,
		SquadBoardCommentCreateDto requestDto) {

		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		SquadBoard squadBoard = squadBoardRepository.findById(squadBoardId)
			.orElseThrow(NotFoundSquadBoard::new);

		if (!squadRepository.isUserOfSquad(squad.getId(), user.getId())) {
			throw new NotSquadUserException();
		}

		squadBoardCommentRepository.save(requestDto.toEntity(squadBoard, user));
	}

	@Transactional
	public void editSquadBoardComment(LoginUser loginUser, Long squadId, Long squadBoardId, Long commentId,
		SquadBoardCommentEditDto requestDto) {
		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		SquadBoard squadBoard = squadBoardRepository.findById(squadBoardId)
			.orElseThrow(NotFoundSquadBoard::new);

		SquadBoardComment comment = squadBoardCommentRepository.findById(commentId)
			.orElseThrow(NotFoundSquadBoardCommentException::new);

		if (!squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			throw new NotSquadUserException();

		if (!squadBoardCommentRepository.isUserOfSquadBoardComment(user.getId(), squadBoard.getId(), comment.getId())) {
			throw new NotSquadBoardCommentUserException();
		}
		comment.edit(requestDto.getSquadBoardCommentContent());
	}

	@Transactional
	public void deleteSquadBoardComment(LoginUser loginUser, Long squadId, Long squadBoardId,
		Long squadBoardCommentId) {

		User user = userRepository.findByEmail(loginUser.getEmail())
			.orElseThrow(UserNotFoundException::new);

		Squad squad = squadRepository.findById(squadId)
			.orElseThrow(SquadNotFoundException::new);

		SquadBoard squadBoard = squadBoardRepository.findById(squadBoardId)
			.orElseThrow(NotFoundSquadBoard::new);

		SquadBoardComment comment = squadBoardCommentRepository.findById(squadBoardCommentId)
			.orElseThrow(NotFoundSquadBoardCommentException::new);

		if (!squadRepository.isUserOfSquad(squad.getId(), user.getId()))
			throw new NotSquadUserException();

		if (!squadBoardCommentRepository.isUserOfSquadBoardComment(user.getId(), squadBoard.getId(), comment.getId()))
			throw new NotSquadBoardCommentUserException();

		squadBoardCommentRepository.deleteById(comment.getId());
	}
}
