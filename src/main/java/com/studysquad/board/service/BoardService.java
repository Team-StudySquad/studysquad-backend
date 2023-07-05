package com.studysquad.board.service;

import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public void write(BoardCreate boardCreate){
        Board board = Board.builder()
                .title(boardCreate.getTitle())
                .content(boardCreate.getContent())
                .build();
        boardRepository.save(board);
    }
}
