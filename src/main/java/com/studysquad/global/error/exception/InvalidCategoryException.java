package com.studysquad.global.error.exception;

public class InvalidCategoryException extends ApiException {

	private static final String MESSAGE = "유효하지 않은 카테고리 입니다";

	public InvalidCategoryException() {
		super(MESSAGE);
	}

	public InvalidCategoryException(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
