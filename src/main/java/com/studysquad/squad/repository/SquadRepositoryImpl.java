package com.studysquad.squad.repository;

import static com.studysquad.category.domain.QCategory.*;
import static com.studysquad.squad.domain.QSquad.*;
import static com.studysquad.usersquad.domain.QUserSquad.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.dto.ProcessSquadDto;
import com.studysquad.squad.dto.QProcessSquadDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SquadRepositoryImpl implements SquadRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public Optional<ProcessSquadDto> getProcessSquad(Long userId) {
		ProcessSquadDto result = queryFactory.select(new QProcessSquadDto(
				squad.id,
				category.categoryName,
				squad.squadName,
				squad.squadExplain))
			.from(squad)
			.join(squad.category, category)
			.join(userSquad).on(squad.id.eq(userSquad.squad.id))
			.where(squad.squadState.eq(SquadStatus.PROCESS)
				.and(userSquad.user.id.eq(userId)))
			.fetchOne();

		return Optional.ofNullable(result);
	}
}
