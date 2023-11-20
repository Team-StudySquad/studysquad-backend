package com.studysquad.global.error.exception;

public class NotFoundProcessMission extends ApiException {

	private static final String MESSAGE = "진행중인 미션을 찾을 수 없습니다";

	public NotFoundProcessMission() {
		super(MESSAGE);
	}

	public NotFoundProcessMission(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
