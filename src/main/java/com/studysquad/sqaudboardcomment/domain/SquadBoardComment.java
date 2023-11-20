package com.studysquad.sqaudboardcomment.domain;

import static javax.persistence.FetchType.*;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.user.domain.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SquadBoardComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "squad_board_comment_id")
	private Long id;
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "squad_board_id")
	private SquadBoard squadBoard;
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	@Column(name = "squad_board_comment_content")
	private String squadBoardCommentContent;
	private LocalDateTime createAt;

	@Builder
	public SquadBoardComment(SquadBoard squadBoard, User user, String squadBoardCommentContent) {
		this.squadBoard = squadBoard;
		this.user = user;
		this.squadBoardCommentContent = squadBoardCommentContent;
		this.createAt = LocalDateTime.now();
	}

	public void edit(String squadBoardCommentContent) {
		this.squadBoardCommentContent = squadBoardCommentContent;
	}
}
