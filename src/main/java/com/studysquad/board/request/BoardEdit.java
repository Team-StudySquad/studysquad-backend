package com.studysquad.board.request;

import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BoardEdit {

	@NotBlank(message = "제목을 입력해주세요")
	private String title;

	@NotBlank(message = "내용을 입력해주세요")
	private String content;

	@Builder
	public BoardEdit(String title, String content) {
		this.title = title;
		this.content = content;
	}
}
