package com.studysquad.usersquad.domain;

import com.studysquad.squad.domain.Squad;
import com.studysquad.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSquad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "squad_id")
    private Squad squad;

    private boolean metored;

    @Builder
    public UserSquad(User user, Squad squad, boolean metored) {
        this.user = user;
        this.squad = squad;
        this.metored = metored;
    }
}
