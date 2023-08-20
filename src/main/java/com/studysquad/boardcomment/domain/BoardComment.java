package com.studysquad.boardcomment.domain;

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

import com.studysquad.board.domain.Board;
import com.studysquad.user.domain.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardComment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "board_comment_id")
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "board_id")
	private Board board;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Lob
	@Column(name = "board_comment_content")
	private String boardCommentContent;

	private LocalDateTime createAt;

	@Builder
	public BoardComment(Board board, User user, String boardCommentContent) {
		this.board = board;
		this.user = user;
		this.boardCommentContent = boardCommentContent;
		this.createAt = LocalDateTime.now();
	}

}
