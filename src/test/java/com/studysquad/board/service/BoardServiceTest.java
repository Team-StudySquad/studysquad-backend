package com.studysquad.board.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;

@SpringBootTest
@AutoConfigureMockMvc
class BoardServiceTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private BoardService boardService;

	@Autowired
	private BoardRepository boardRepository;

	@BeforeEach
	void clear(){
		boardRepository.deleteAll();
	}

	@Test
	@DisplayName("글 작성")
	void test1(){
		//given
		BoardCreate boardCreate = BoardCreate.builder()
			.title("제목입니다1")
			.content("내용입니다1")
			.build();

		//when
		boardService.write(boardCreate);

		//then
		assertEquals(1L, boardRepository.count());

		Board board = boardRepository.findAll().get(0);
		assertEquals(board.getTitle(), "제목입니다1");
		assertEquals(board.getContent(), "내용입니다1");
	}

	@Test
	@DisplayName("글 1개 조회")
	void test2(){
		//given
		Board request = Board.builder()
			.title("제목입니다2")
			.content("내용입니다2")
			.build();
		boardRepository.save(request);
		//when
		Board board = boardService.get(request.getId());
		//then
		assertNotNull(board);
		assertEquals("제목입니다2", board.getTitle());
		assertEquals("내용입니다2", board.getContent());
	}





}