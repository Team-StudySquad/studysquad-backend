package com.studysquad.global.security;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RefreshToken {
	private String header;
	private String data;
	private int expirationPeriod;

	@Builder
	public RefreshToken(String header, String data, int expirationPeriod) {
		this.header = header;
		this.data = data;
		this.expirationPeriod = expirationPeriod;
	}

}
