package com.studysquad.global.error.exception;

public class MentorAlreadyExistException extends ApiException {

	private static final String MESSAGE = "멘토가 이미 존재하는 스쿼드 입니다";

	public MentorAlreadyExistException() {
		super(MESSAGE);
	}

	public MentorAlreadyExistException(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
