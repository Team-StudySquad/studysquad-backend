package com.studysquad.board.controller;

import com.studysquad.board.request.BoardCreate;
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
    public void board(@RequestBody BoardCreate boardCreate){
        boardService.write(boardCreate);
    }

    @GetMapping("/boards/{boardId}")
    public void get(@PathVariable Long boardId){
        boardService.get(boardId);
    }


}
