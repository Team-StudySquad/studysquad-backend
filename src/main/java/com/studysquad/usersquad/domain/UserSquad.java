package com.studysquad.usersquad.domain;

import static javax.persistence.FetchType.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.studysquad.squad.domain.Squad;
import com.studysquad.user.domain.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSquad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_squad_id")
	private Long id;
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "squad_id")
	private Squad squad;
	private boolean isMentor;
	private boolean isCreator;

	@Builder
	public UserSquad(User user, Squad squad, boolean isMentor, boolean isCreator) {
		this.user = user;
		this.squad = squad;
		this.isMentor = isMentor;
		this.isCreator = isCreator;
	}
}
