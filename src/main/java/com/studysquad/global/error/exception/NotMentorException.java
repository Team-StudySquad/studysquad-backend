package com.studysquad.global.error.exception;

public class NotMentorException extends ApiException {

	private static final String MESSAGE = "멘토가 아닌 사용자 입니다";

	public NotMentorException() {
		super(MESSAGE);
	}

	public NotMentorException(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
