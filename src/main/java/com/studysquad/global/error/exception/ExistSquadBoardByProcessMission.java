package com.studysquad.global.error.exception;

public class ExistSquadBoardByProcessMission extends ApiException {

	private static final String MESSAGE = "이미 게시글을 작성하였습니다";

	public ExistSquadBoardByProcessMission() {
		super(MESSAGE);
	}

	public ExistSquadBoardByProcessMission(String field, String message) {
		super(MESSAGE);
		addValidation(field, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
