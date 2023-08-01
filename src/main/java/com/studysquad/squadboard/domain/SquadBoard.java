package com.studysquad.squadboard.domain;

import static javax.persistence.FetchType.*;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.studysquad.mission.domain.Mission;
import com.studysquad.squad.domain.Squad;
import com.studysquad.user.domain.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SquadBoard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "squad_board_id")
	private Long id;
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "squad_id")
	private Squad squad;
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "mission_id")
	private Mission mission;
	private String squadBoardTitle;
	private String squadBoardContent;
	private LocalDateTime createAt;

	@Builder
	public SquadBoard(User user, Squad squad, Mission mission, String squadBoardTitle, String squadBoardContent) {
		this.user = user;
		this.squad = squad;
		this.mission = mission;
		this.squadBoardTitle = squadBoardTitle;
		this.squadBoardContent = squadBoardContent;
		this.createAt = LocalDateTime.now();
	}
}
