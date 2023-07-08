package com.studysquad.global.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

@Component
@Getter
public class JwtProvider {

	private final Key secretKey;
	private final Long accessTokenExpirationPeriod;
	private final String accessHeader;
	private final Long refreshTokenExpirationPeriod;
	private final String refreshHeader;
	private static final String BEARER = "Bearer ";

	public JwtProvider(@Value("${jwt.secretKey}") String secretKey,
		@Value("${jwt.access.expiration}") Long accessTokenExpirationPeriod,
		@Value("${jwt.access.header}") String accessHeader,
		@Value("${jwt.refresh.expiration}") Long refreshTokenExpirationPeriod,
		@Value("${jwt.refresh.header}") String refreshHeader) {

		this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpirationPeriod = accessTokenExpirationPeriod;
		this.accessHeader = accessHeader;
		this.refreshTokenExpirationPeriod = refreshTokenExpirationPeriod;
		this.refreshHeader = refreshHeader;
	}

	public Token createToken(String email) {
		AccessToken accessToken = AccessToken.builder()
			.header(accessHeader)
			.data(createAccessToken(email))
			.build();
		RefreshToken refreshToken = RefreshToken.builder()
			.header(refreshHeader)
			.data(createRefreshToken())
			.build();

		return Token.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	public Optional<String> extractToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(accessHeader))
			.filter(token -> token.startsWith(BEARER))
			.map(token -> token.replace(BEARER, ""));
	}

	public String getUsernameFromToken(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}

	public boolean isTokenValid(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private String createAccessToken(String email) {
		return Jwts.builder()
			.setSubject(email)
			.setExpiration(expireTime(accessTokenExpirationPeriod))
			.signWith(secretKey)
			.compact();
	}

	private String createRefreshToken() {
		return Jwts.builder()
			.setExpiration(expireTime(refreshTokenExpirationPeriod))
			.signWith(secretKey)
			.compact();
	}

	private Date expireTime(Long expirationPeriod) {
		return new Date(System.currentTimeMillis() + expirationPeriod);
	}

}
