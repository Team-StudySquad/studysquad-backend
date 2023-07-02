package com.studysquad.global.error.exception;

public class InvalidSigningInformation extends ApiException {

	private static final String MESSAGE = "아이디/비밀번호가 일치하지 않습니다";

	public InvalidSigningInformation() {
		super(MESSAGE);
	}

	public InvalidSigningInformation(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 401;
	}
}
