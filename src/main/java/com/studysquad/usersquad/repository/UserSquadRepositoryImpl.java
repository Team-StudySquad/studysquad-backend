package com.studysquad.usersquad.repository;

import static com.studysquad.squad.domain.QSquad.*;
import static com.studysquad.usersquad.domain.QUserSquad.*;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.squad.domain.SquadStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserSquadRepositoryImpl implements UserSquadRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Boolean hasActiveSquadByUserId(Long userId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(userSquad)
			.innerJoin(userSquad.squad, squad)
			.where(squad.squadState.eq(SquadStatus.PROCESS)
				.or(squad.squadState.eq(SquadStatus.RECRUIT)))
			.fetchFirst();

		return fetchOne != null;
	}

	@Override
	public Boolean hasMentorBySquadId(Long squadId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(userSquad)
			.innerJoin(userSquad.squad)
			.where(userSquad.squad.id.eq(squadId)
				.and(userSquad.isMentor.isTrue()))
			.fetchOne();

		return fetchOne != null;
	}
}
