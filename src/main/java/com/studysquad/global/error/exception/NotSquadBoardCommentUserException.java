package com.studysquad.global.error.exception;

public class NotSquadBoardCommentUserException extends ApiException {

	private static final String MESSAGE = "스쿼드 게시글 댓글을 작성한 사용자가 아닙니다";

	public NotSquadBoardCommentUserException() {
		super(MESSAGE);
	}

	public NotSquadBoardCommentUserException(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
