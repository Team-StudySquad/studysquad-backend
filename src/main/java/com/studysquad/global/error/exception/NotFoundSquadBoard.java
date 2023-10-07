package com.studysquad.global.error.exception;

public class NotFoundSquadBoard extends ApiException {

	private static final String MESSAGE = "스쿼드 게시글을 찾을 수 없습니다.";

	public NotFoundSquadBoard() {
		super(MESSAGE);
	}

	public NotFoundSquadBoard(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
