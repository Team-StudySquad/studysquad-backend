package com.studysquad.global.error.exception;

public class InvalidLoginUserException extends ApiException {

	private static final String MESSAGE = "유효하지 않는 로그인 정보입니다";

	public InvalidLoginUserException() {
		super(MESSAGE);
	}

	public InvalidLoginUserException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 401;
	}
}
