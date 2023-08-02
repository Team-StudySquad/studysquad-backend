package com.studysquad.mission.domain;

import static javax.persistence.FetchType.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.studysquad.board.domain.Board;
import com.studysquad.squad.domain.Squad;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mission_id")
	private Long id;
	private String missionTitle;
	private String missionContent;
	@Enumerated(EnumType.STRING)
	private MissionStatus missionStatus;
	private int missionSequence;
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "squad_id")
	private Squad squad;
	@OneToOne(fetch = LAZY)
	@JoinColumn(name = "board_id")
	private Board board;

	@Builder
	public Mission(String missionTitle, String missionContent, MissionStatus missionStatus, int missionSequence,
		Squad squad, Board board) {
		this.missionTitle = missionTitle;
		this.missionContent = missionContent;
		this.missionStatus = missionStatus;
		this.missionSequence = missionSequence;
		this.squad = squad;
		this.board = board;
	}
}
