package com.studysquad.board.domain;

import static javax.persistence.FetchType.*;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.studysquad.mission.domain.Mission;
import com.studysquad.squad.domain.Squad;
import com.studysquad.user.domain.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "board_id")
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "squad_id")
	private Squad squad;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@OneToOne(fetch = LAZY)
	@JoinColumn(name = "mission_id")
	private Mission mission;

	private String title;

	@Lob
	private String content;

	private LocalDateTime createAt;

	@Builder
	public Board(Squad squad, User user, Mission mission, String title, String content) {
		this.squad = squad;
		this.user = user;
		this.mission = mission;
		this.title = title;
		this.content = content;
		this.createAt = LocalDateTime.now();
	}

    public void edit(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
