package com.studysquad.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studysquad.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByRefreshToken(String refreshToken);

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);
}
