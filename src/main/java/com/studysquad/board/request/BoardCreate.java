package com.studysquad.board.request;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@ToString
public class BoardCreate {

    @NotBlank(message = "제목을 입력해주세요")
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    @Builder
    public BoardCreate(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
