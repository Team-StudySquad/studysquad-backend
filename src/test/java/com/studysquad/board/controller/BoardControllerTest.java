package com.studysquad.board.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.*;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.mission.domain.Mission;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private BoardRepository boardRepository;

	@BeforeEach
	void clear(){
		boardRepository.deleteAll();
	}

	@Test
	@DisplayName("/boards 요청시 글 생성")
	void test1() throws Exception {
		//given
		BoardCreate request = BoardCreate.builder()
			.title("제목입니다1")
			.content("내용입니다1")
			.build();
		String json = objectMapper.writeValueAsString(request);
		//when
		mockMvc.perform(MockMvcRequestBuilders.post("/boards")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
			)
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print());
		//then
		assertEquals(1L, boardRepository.count());

		Board board = boardRepository.findAll().get(0);
		assertEquals(board.getTitle(), "제목입니다1");
		assertEquals(board.getContent(), "내용입니다1");
	}

	@Test
	@DisplayName("boardId를 통한 글 조회")
	@Transactional
	void test2() throws Exception{
		Board board = Board.builder()
			.title("제목입니다2")
			.content("내용입니다2")
			.build();

		boardRepository.save(board);


		mockMvc.perform(MockMvcRequestBuilders.get("/boards/{boardId}", board.getId())
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(board.getId()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.title").value("제목입니다2"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.content").value("내용입니다2"))
			.andDo(MockMvcResultHandlers.print());
	}



}