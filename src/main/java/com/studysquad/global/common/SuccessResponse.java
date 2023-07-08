package com.studysquad.global.common;

import org.springframework.lang.Nullable;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SuccessResponse<T> {
	private int status;
	private String message;
	private T data;

	@Builder
	public SuccessResponse(int status, String message, @Nullable T data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}
}
