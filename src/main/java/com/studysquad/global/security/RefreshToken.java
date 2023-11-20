package com.studysquad.global.security;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RefreshToken {
	private String header;
	private String data;
	public static final int EXPIRATION_PERIOD = 1209600;

	@Builder
	public RefreshToken(String header, String data) {
		this.header = header;
		this.data = data;
	}

}
