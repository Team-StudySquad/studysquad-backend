package com.studysquad.user.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;
	private String email;
	private String password;
	private String nickname;
	@Enumerated(EnumType.STRING)
	private Role role;
	private String refreshToken;

	@Builder
	public User(String email, String password, String nickname, Role role, String refreshToken) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.role = role;
		this.refreshToken = refreshToken;
	}

	public void updateRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void invalidateRefreshToken() {
		this.refreshToken = null;
	}
}
