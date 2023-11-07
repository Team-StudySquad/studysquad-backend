package com.studysquad.global.error.exception;

public class NotFoundSquadBoardCommentException extends ApiException {

	private static final String MESSAGE = "해당 댓글을 찾을 수 없습니다";

	public NotFoundSquadBoardCommentException() {
		super(MESSAGE);
	}

	public NotFoundSquadBoardCommentException(String fieldName, String message) {
		super(MESSAGE);
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
