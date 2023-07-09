package com.studysquad.board.service;

import java.util.List;
import java.util.stream.Collectors;

import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.response.BoardResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public BoardResponse get(Long id){
        Board response = boardRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        return BoardResponse.builder()
            .id(response.getId())
            .title(response.getTitle())
            .content(response.getContent())
            .build();
    }

    public List<BoardResponse> getAllBoards(Pageable pageable){
        return boardRepository.findAll(pageable).stream()
            .map(BoardResponse::new)
            .collect(Collectors.toList());
    }
}
