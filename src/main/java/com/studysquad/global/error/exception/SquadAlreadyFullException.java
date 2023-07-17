package com.studysquad.global.error.exception;

public class SquadAlreadyFullException extends ApiException {

	private static final String MESSAGE = "모집이 완료된 스쿼드 입니다";

	public SquadAlreadyFullException() {
		super(MESSAGE);
	}

	public SquadAlreadyFullException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
