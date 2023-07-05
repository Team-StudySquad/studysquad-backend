package com.studysquad.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.studysquad.user.domain.Role;
import com.studysquad.user.domain.User;
import com.studysquad.user.repository.UserRepository;
import com.studysquad.user.service.ApiUserDetailsService;

class ApiUserDetailsServiceTest {

	@Mock
	UserRepository mockRepository;

	@InjectMocks
	ApiUserDetailsService apiUserDetailsService;

	@BeforeEach
	void init() {
		mockRepository = mock(UserRepository.class);
		apiUserDetailsService = new ApiUserDetailsService(mockRepository);
	}

	@Test
	@DisplayName("올바른 이메일로 사용자를 조회")
	void loadByUserName_validEmail() {
		User user = User.builder()
			.email("aaa@aaa.com")
			.password("password")
			.role(Role.USER)
			.build();
		when(mockRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		UserDetails result = apiUserDetailsService.loadUserByUsername(user.getEmail());

		assertThat(user.getEmail()).isEqualTo(result.getUsername());
		assertThat(user.getPassword()).isEqualTo(result.getPassword());
		assertThat(result.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_" + user.getRole())))
			.isTrue();
	}

	@Test
	@DisplayName("잘못된 이메일로 사용자를 조회")
	void loadByUserName_invalidEmail() {
		String invalidEmail = "invalid@aaa.com";

		when(mockRepository.findByEmail(invalidEmail)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> apiUserDetailsService.loadUserByUsername(invalidEmail))
			.isInstanceOf(UsernameNotFoundException.class);
	}
}