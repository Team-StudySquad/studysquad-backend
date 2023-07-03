package com.studysquad.user.dto;

import javax.validation.constraints.NotBlank;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;

import lombok.Builder;
import lombok.Getter;

@Getter
public class JoinRequestDto {

	@NotBlank(message = "이메일을 입력해주세요")
	private String email;
	@NotBlank(message = "비밀번호를 입력해주세요")
	private String password;
	@NotBlank(message = "닉네임을 입력해주세요")
	private String nickname;

	@Builder
	public JoinRequestDto(String email, String password, String nickname) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
	}

	public void passwordEncryption(PasswordEncoder passwordEncoder) {
		this.password = passwordEncoder.encode(this.password);
	}

	public User toEntity() {
		return User.builder()
			.email(email)
			.password(password)
			.nickname(nickname)
			.role(Role.USER)
			.build();
	}
}
