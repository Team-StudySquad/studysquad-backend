package com.studysquad.category.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studysquad.category.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	Optional<Category> findByCategoryName(String name);
}
