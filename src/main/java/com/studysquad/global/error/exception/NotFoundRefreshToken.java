package com.studysquad.global.error.exception;

public class NotFoundRefreshToken extends ApiException {

	private static final String MESSAGE = "Refresh 토큰을 찾을 수 없습니다";

	public NotFoundRefreshToken() {
		super(MESSAGE);
	}

	public NotFoundRefreshToken(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 401;
	}
}
