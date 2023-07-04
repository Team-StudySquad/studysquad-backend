package com.studysquad.global.error.exception;

public class InvalidTokenException extends ApiException {

	private static final String MESSAGE = "유효하지 않는 토큰입니다";

	public InvalidTokenException() {
		super(MESSAGE);
	}

	public InvalidTokenException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 401;
	}
}
