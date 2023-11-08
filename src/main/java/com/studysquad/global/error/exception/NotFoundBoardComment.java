package com.studysquad.global.error.exception;

public class NotFoundBoardComment extends ApiException {

	private static final String MESSAGE = "게시글 댓글을 찾을 수 없습니다";

	public NotFoundBoardComment() {
		super(MESSAGE);
	}

	public NotFoundBoardComment(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
