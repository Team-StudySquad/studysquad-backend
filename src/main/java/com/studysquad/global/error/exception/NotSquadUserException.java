package com.studysquad.global.error.exception;

public class NotSquadUserException extends ApiException {

	private static final String MESSAGE = "스쿼드에 속한 사용자가 아닙니다";

	public NotSquadUserException() {
		super(MESSAGE);
	}

	public NotSquadUserException(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
