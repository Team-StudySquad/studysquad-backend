package com.studysquad.docs.util;

import com.studysquad.global.security.AccessToken;
import com.studysquad.global.security.RefreshToken;
import com.studysquad.global.security.Token;

public class TokenFactory {

	public static Token createToken() {
		return Token.builder()
			.accessToken(createAccessToken())
			.refreshToken(createRefreshToken())
			.build();
	}

	private static AccessToken createAccessToken() {
		return AccessToken.builder()
			.header("Authorization")
			.data("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVJ9.access.token")
			.build();
	}

	private static RefreshToken createRefreshToken() {
		return RefreshToken.builder()
			.header("Authorization-refresh")
			.data("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVJ9.refresh.token")
			.build();
	}
}
