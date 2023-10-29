package com.studysquad.boardcomment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studysquad.boardcomment.dto.BoardCommentResponse;
import com.studysquad.boardcomment.repository.BoardCommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardCommentService {

	private final BoardCommentRepository boardCommentRepository;

	public List<BoardCommentResponse> getBoardComments(Long boardId) {
		return boardCommentRepository.getBoardComments(boardId);
	}
}
