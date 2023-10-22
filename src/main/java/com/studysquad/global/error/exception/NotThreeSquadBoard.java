package com.studysquad.global.error.exception;

public class NotThreeSquadBoard extends ApiException{
	private static final String MESSAGE = "스쿼드 게시물이 3개가 아닙니다.";

	public NotThreeSquadBoard() {
		super(MESSAGE);
	}

	public NotThreeSquadBoard(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
