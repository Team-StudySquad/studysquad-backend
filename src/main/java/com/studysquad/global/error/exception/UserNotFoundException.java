package com.studysquad.global.error.exception;

public class UserNotFoundException extends ApiException {

	private static final String MESSAGE = "사용자를 찾을 수 없습니다";

	public UserNotFoundException() {
		super(MESSAGE);
	}

	public UserNotFoundException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
