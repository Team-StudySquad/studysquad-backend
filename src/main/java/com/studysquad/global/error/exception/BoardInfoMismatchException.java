package com.studysquad.global.error.exception;

public class BoardInfoMismatchException extends ApiException {
	private static final String MESSAGE = "게시글 정보가 일치하지 않습니다";

	public BoardInfoMismatchException() {
		super(MESSAGE);
	}

	public BoardInfoMismatchException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
