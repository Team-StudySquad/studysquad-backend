package com.studysquad.user.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.studysquad.board.domain.Board;
import com.studysquad.boardcomment.domain.BoardComment;
import com.studysquad.sqaudboardcomment.domain.SquadBoardComment;
import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.usersquad.domain.UserSquad;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;
	private String email;
	private String password;
	private String nickname;
	@Enumerated(EnumType.STRING)
	private Role role;
	private String refreshToken;
	@OneToMany(mappedBy = "user")
	private List<UserSquad> userSquads = new ArrayList<>();
	@OneToMany(mappedBy = "user")
	private List<SquadBoard> squadBoards = new ArrayList<>();
	@OneToMany(mappedBy = "user")
	private List<SquadBoardComment> squadBoardComments = new ArrayList<>();
	@OneToMany(mappedBy = "user")
	private List<Board> boards = new ArrayList<>();
	@OneToMany(mappedBy = "user")
	private List<BoardComment> boardComments = new ArrayList<>();

	@Builder
	public User(String email, String password, String nickname, Role role, String refreshToken) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.role = role;
		this.refreshToken = refreshToken;
	}
}
