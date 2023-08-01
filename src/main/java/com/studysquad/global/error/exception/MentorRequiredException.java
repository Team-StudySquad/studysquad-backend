package com.studysquad.global.error.exception;

public class MentorRequiredException extends ApiException {

	private static final String MESSAGE = "스쿼드 내에 멘토가 필요합니다";

	public MentorRequiredException() {
		super(MESSAGE);
	}

	public MentorRequiredException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
