package com.studysquad.global.error.exception;

public class MissionNotFoundException extends ApiException {

	private static final String MESSAGE = "미션을 찾을 수 없습니다";

	public MissionNotFoundException() {
		super(MESSAGE);
	}

	public MissionNotFoundException(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
