package com.studysquad.global.error.exception;

public class SquadNotProgressException extends ApiException {

	private static final String MESSAGE = "스쿼드가 진행중이지 않습니다";

	public SquadNotProgressException() {
		super(MESSAGE);
	}

	public SquadNotProgressException(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
