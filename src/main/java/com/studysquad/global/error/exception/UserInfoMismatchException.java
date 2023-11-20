package com.studysquad.global.error.exception;

public class UserInfoMismatchException extends ApiException {

	private static final String MESSAGE = "사용자 정보가 일치하지 않습니다";

	public UserInfoMismatchException() {
		super(MESSAGE);
	}

	public UserInfoMismatchException(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
