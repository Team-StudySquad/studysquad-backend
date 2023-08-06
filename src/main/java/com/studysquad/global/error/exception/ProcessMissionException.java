package com.studysquad.global.error.exception;

public class ProcessMissionException extends ApiException {

	private static final String MESSAGE = "진행중인 미션 입니다";

	public ProcessMissionException() {
		super(MESSAGE);
	}

	public ProcessMissionException(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
