package com.studysquad.global.error.exception;

public class ExistActiveSquadException extends ApiException {

	private static final String MESSAGE = "이미 활성화된 스쿼드가 존재 합니다";

	public ExistActiveSquadException() {
		super(MESSAGE);
	}

	public ExistActiveSquadException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 409;
	}
}
