package com.studysquad.global.common;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FailResponse {

	private int status;
	private String message;
	private Map<String, String> validation;

	@Builder
	public FailResponse(int status, String message, Map<String, String> validation) {
		this.status = status;
		this.message = message;
		this.validation = validation != null ? validation : new HashMap<>();
	}

	public void addValidation(String field, String errorMessage) {
		this.validation.put(field, errorMessage);
	}
}
