package com.studysquad.user.dto;

import com.studysquad.user.domain.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginUser {
	private String email;
	private Role role;

	@Builder
	public LoginUser(String email, Role role) {
		this.email = email;
		this.role = role;
	}
}
