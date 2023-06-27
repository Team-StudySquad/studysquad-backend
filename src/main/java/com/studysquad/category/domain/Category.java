package com.studysquad.category.domain;

import com.studysquad.squad.domain.Squad;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    private String categoryName;

    @OneToMany(mappedBy = "category")
    private List<Squad> squads = new ArrayList<>();

    @Builder
    public Category(String categoryName) {
        this.categoryName = categoryName;
    }
}
