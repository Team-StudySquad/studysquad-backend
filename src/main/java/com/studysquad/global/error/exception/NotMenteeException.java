package com.studysquad.global.error.exception;

public class NotMenteeException extends ApiException {

	private static final String MESSAGE = "멘티가 아닌 사용자입니다";

	public NotMenteeException() {
		super(MESSAGE);
	}

	public NotMenteeException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}

}
