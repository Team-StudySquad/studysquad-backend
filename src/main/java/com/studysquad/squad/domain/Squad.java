package com.studysquad.squad.domain;

import static javax.persistence.FetchType.*;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.studysquad.category.domain.Category;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
	private SquadStatus squadStatus;
	private LocalDateTime createAt;

	@Builder
	public Squad(Category category, String squadName, String squadExplain, SquadStatus squadStatus,
		LocalDateTime createAt) {
		this.category = category;
		this.squadName = squadName;
		this.squadExplain = squadExplain;
		this.squadStatus = squadStatus;
		this.createAt = createAt;
	}

	public void updateStatus(SquadStatus status) {
		this.squadStatus = status;
	}
}
