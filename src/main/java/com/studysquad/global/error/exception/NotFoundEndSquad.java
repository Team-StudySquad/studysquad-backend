package com.studysquad.global.error.exception;

public class NotFoundEndSquad extends ApiException {

	private static final String MESSAGE = "종료된 스쿼드를 찾을 수 없습니다";

	public NotFoundEndSquad() {
		super(MESSAGE);
	}

	public NotFoundEndSquad(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
