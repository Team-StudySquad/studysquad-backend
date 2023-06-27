package com.studysquad.board.domain;

import com.studysquad.boardcomment.domain.BoardComment;
import com.studysquad.mission.domain.Mission;
import com.studysquad.squad.domain.Squad;
import com.studysquad.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

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

    @OneToMany(mappedBy = "board")
    private List<BoardComment> boardComments = new ArrayList<>();

    @Builder
    public Board(Long id, Squad squad, User user, Mission mission, String title, String content) {
        this.squad = squad;
        this.user = user;
        this.mission = mission;
        this.title = title;
        this.content = content;
        this.createAt = LocalDateTime.now();
    }

}
