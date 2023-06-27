package com.studysquad.squad.domain;

import com.studysquad.category.domain.Category;
import com.studysquad.squadboard.domain.SquadBoard;
import com.studysquad.usersquad.domain.UserSquad;
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
public class Squad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "squad_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    private String squadName;
    private String squadExplain;

    @Enumerated(EnumType.STRING)
    private SquadStatus squadState;
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "squad")
    private List<UserSquad> userSquads = new ArrayList<>();

    @OneToMany(mappedBy = "squad")
    private List<SquadBoard> squadBoards = new ArrayList<>();

    @Builder
    public Squad(Category category, String squadName, String squadExplain, SquadStatus squadState) {
        this.category = category;
        this.squadName = squadName;
        this.squadExplain = squadExplain;
        this.squadState = squadState;
        this.createAt = LocalDateTime.now();
    }
}
