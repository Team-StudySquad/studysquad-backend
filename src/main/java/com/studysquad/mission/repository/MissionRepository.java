package com.studysquad.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studysquad.mission.domain.Mission;

public interface MissionRepository extends JpaRepository<Mission, Long> {
}
