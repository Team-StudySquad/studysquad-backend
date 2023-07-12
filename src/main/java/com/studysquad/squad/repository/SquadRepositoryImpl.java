package com.studysquad.squad.repository;

import static com.studysquad.squad.domain.QSquad.*;
import static com.studysquad.usersquad.domain.QUserSquad.*;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studysquad.squad.domain.SquadStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SquadRepositoryImpl implements SquadRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public Boolean isUserInActiveSquad(Long userId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(userSquad)
			.where(userSquad.user.id.eq(userId)
				.and(squad.squadState.eq(SquadStatus.PROCESS)
					.or(squad.squadState.eq(SquadStatus.RECRUIT))))
			.fetchFirst();

		return fetchOne != null;
	}
}
