package com.studysquad.global.error.exception;

public class NotFoundBoard extends ApiException {

	private static final String MESSAGE = "게시글을 찾을 수 없습니다";

	public NotFoundBoard() {
		super(MESSAGE);
	}

	public NotFoundBoard(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
