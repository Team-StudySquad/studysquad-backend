package com.studysquad.board.controller;

import static org.junit.jupiter.api.Assertions.*;

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
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;

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
	@DisplayName("/board 요청시 db에 값이 저장된다. 단건 조회")
	public void test1() throws Exception {
		//given
		BoardCreate request = BoardCreate.builder()
			.title("제목입니다1")
			.content("내용입니다1")
			.build();

		String json = objectMapper.writeValueAsString(request);
		mockMvc.perform(MockMvcRequestBuilders.post("/boards")
			.content(json)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.title").value(request.getTitle()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.content").value(request.getContent()))
			.andDo(MockMvcResultHandlers.print());
	}

}