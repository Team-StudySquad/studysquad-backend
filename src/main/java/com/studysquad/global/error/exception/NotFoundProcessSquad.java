package com.studysquad.global.error.exception;

public class NotFoundProcessSquad extends ApiException {

	private static final String MESSAGE = "진행중인 스쿼드를 찾을 수 없습니다";

	public NotFoundProcessSquad() {
		super(MESSAGE);
	}

	public NotFoundProcessSquad(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
