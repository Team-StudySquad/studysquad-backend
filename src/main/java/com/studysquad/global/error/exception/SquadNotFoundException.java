package com.studysquad.global.error.exception;

public class SquadNotFoundException extends ApiException {

	private static final String MESSAGE = "존재하지 않는 스쿼드 입니다";

	public SquadNotFoundException() {
		super(MESSAGE);
	}

	public SquadNotFoundException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
