package com.studysquad.board.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.transaction.Transactional;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
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
import com.studysquad.board.request.BoardEdit;
import com.studysquad.board.response.BoardResponse;
import com.studysquad.mission.domain.Mission;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
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

	@Test
	@DisplayName("글 1페이지 조회")
	void test3() throws Exception{
		//given
		List<Board> requestBoards = IntStream.rangeClosed(1, 30)
			.mapToObj(i -> Board.builder()
				.title("제목 " + i)
				.content("내용 " + i)
				.build())
			.collect(Collectors.toList());

		boardRepository.saveAll(requestBoards);

		mockMvc.perform(MockMvcRequestBuilders.get("/boards?page=0&sort=id,desc")
			.contentType(APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.is(5)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(30))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("제목 30"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].content").value("내용 30"))
			.andDo(MockMvcResultHandlers.print());
	}


	@Test
	@DisplayName("글 제목 수정")
	void test4() throws Exception{
		//given
		Board board = Board.builder()
			.title("제목입니다1")
			.content("내용입니다1")
			.build();

		boardRepository.save(board);

		BoardEdit boardEdit = BoardEdit.builder()
			.title("제목입니다2")
			.content("내용입니다2")
			.build();

		//expected
		mockMvc.perform(MockMvcRequestBuilders.patch("/boards/{boardId}", board.getId())
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(boardEdit)))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print());
	}


	@Test
	@DisplayName("게시글 삭제")
	void test8() throws Exception {
		//given
		Board board = Board.builder()
			.title("제목입니다1")
			.content("내용입니다1")
			.build();
		boardRepository.save(board);

		mockMvc.perform(MockMvcRequestBuilders.delete("/boards/{boardId}", board.getId())
			.contentType(APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print());
	}




}