package com.studysquad.usersquad.repository;

import static com.studysquad.squad.domain.QSquad.*;
import static com.studysquad.usersquad.domain.QUserSquad.*;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.squad.domain.SquadStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSquadRepositoryImpl implements UserSquadRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Boolean hasActiveSquadByUserId(Long userId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(userSquad)
			.innerJoin(userSquad.squad, squad)
			.where(userSquad.user.id.eq(userId)
				.and(squad.squadState.eq(SquadStatus.PROCESS)
					.or(squad.squadState.eq(SquadStatus.RECRUIT))))
			.fetchFirst();

		return fetchOne != null;
	}
}
