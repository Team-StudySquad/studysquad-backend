package com.studysquad.global.security;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Token {
	private AccessToken accessToken;
	private RefreshToken refreshToken;

	@Builder
	public Token(AccessToken accessToken, RefreshToken refreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}
}
