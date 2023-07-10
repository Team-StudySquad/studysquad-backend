package com.studysquad.board.controller;

import java.util.List;

import javax.validation.Valid;

import com.studysquad.board.domain.Board;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.request.BoardEdit;
import com.studysquad.board.response.BoardResponse;
import com.studysquad.board.service.BoardService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    public BoardResponse get(@PathVariable Long boardId){
         return boardService.get(boardId);
    }

    @GetMapping("/boards")
    public List<BoardResponse> getAllBoards(@PageableDefault(size = 5) Pageable pageable){
        return boardService.getAllBoards(pageable);
    }

    @PatchMapping("/boards/{boardId}")
    public void edit(@PathVariable Long boardId, @RequestBody @Valid BoardEdit request){
        boardService.edit(boardId, request);
    }

    @DeleteMapping("/boards/{boardId}")
    public void delete(@PathVariable Long boardId){
        boardService.delete(boardId);
    }


}
