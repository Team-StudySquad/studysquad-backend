package com.studysquad.global.error.exception;

public class DuplicateNicknameException extends ApiException {

	private static final String MESSAGE = "중복된 닉네임 입니다";

	public DuplicateNicknameException() {
		super(MESSAGE);
	}

	public DuplicateNicknameException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 409;
	}
}
