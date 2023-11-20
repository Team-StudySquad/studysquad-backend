package com.studysquad.global.security;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AccessToken {
	private String header;
	private String data;

	@Builder
	public AccessToken(String header, String data) {
		this.header = header;
		this.data = data;
	}
}
