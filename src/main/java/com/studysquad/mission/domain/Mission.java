package com.studysquad.mission.domain;

import com.studysquad.board.domain.Board;
import com.studysquad.squad.domain.Squad;
import com.studysquad.squadboard.domain.SquadBoard;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

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
    @JoinColumn(name =  "squad_id")
    private Squad squad;

    @OneToMany(mappedBy = "mission")
    private List<SquadBoard> squadBoards = new ArrayList<>();

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Builder
    public Mission(String missionTitle, String missionContent, MissionStatus missionStatus, int missionSequence) {
        this.missionTitle = missionTitle;
        this.missionContent = missionContent;
        this.missionStatus = missionStatus;
        this.missionSequence = missionSequence;
    }
}
