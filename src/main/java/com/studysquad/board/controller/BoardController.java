package com.studysquad.board.controller;

import javax.validation.Valid;

import com.studysquad.board.domain.Board;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.response.BoardResponse;
import com.studysquad.board.service.BoardService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/boards")
    public void board(@RequestBody @Valid BoardCreate boardCreate){
        boardService.write(boardCreate);
    }

    @GetMapping("/boards/{boardId}")
    public Board get(@PathVariable Long boardId){
         return boardService.get(boardId);
    }


}
