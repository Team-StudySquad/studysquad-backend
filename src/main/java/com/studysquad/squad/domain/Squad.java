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
import com.studysquad.squad.dto.SquadCreateDto;

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
	private SquadStatus squadState;
	private LocalDateTime createAt;

	@Builder
	public Squad(Category category, String squadName, String squadExplain, SquadStatus squadState,
		LocalDateTime createAt) {
		this.category = category;
		this.squadName = squadName;
		this.squadExplain = squadExplain;
		this.squadState = squadState;
		this.createAt = createAt;
	}

	public static Squad createSquad(Category category, SquadCreateDto squadCreateDto) {
		return Squad.builder()
			.category(category)
			.squadName(squadCreateDto.getSquadName())
			.squadExplain(squadCreateDto.getSquadExplain())
			.squadState(SquadStatus.RECRUIT)
			.createAt(LocalDateTime.now())
			.build();
	}

	public void updateStatus(SquadStatus status) {
		this.squadState = status;
	}
}
