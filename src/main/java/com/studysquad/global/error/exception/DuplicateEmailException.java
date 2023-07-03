package com.studysquad.global.error.exception;

public class DuplicateEmailException extends ApiException {

	private static final String MESSAGE = "중복된 이메일 입니다";

	public DuplicateEmailException() {
		super(MESSAGE);
	}

	public DuplicateEmailException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 409;
	}
}
