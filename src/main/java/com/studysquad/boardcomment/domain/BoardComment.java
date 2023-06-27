package com.studysquad.boardcomment.domain;

import com.studysquad.board.domain.Board;
import com.studysquad.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "borad_comment_id")
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
