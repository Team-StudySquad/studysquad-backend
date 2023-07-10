package com.studysquad.board.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.board.domain.Board;
import com.studysquad.board.repository.BoardRepository;
import com.studysquad.board.request.BoardCreate;
import com.studysquad.board.request.BoardEdit;
import com.studysquad.board.response.BoardResponse;

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
		BoardResponse response= boardService.get(request.getId());
		//then
		assertNotNull(response);
		assertEquals("제목입니다2", response.getTitle());
		assertEquals("내용입니다2", response.getContent());
	}

	@Test
	@DisplayName("글 1페이지 조회")
	void test3(){
		//given
		List<Board> reqeustBoards = IntStream.rangeClosed(1, 30)
			.mapToObj(i -> Board.builder()
				.title("제목 " + i)
				.content("내용 " + i)
				.build())
			.collect(Collectors.toList());

		boardRepository.saveAll(reqeustBoards);

		Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));

		//when
		List<BoardResponse> boards = boardService.getAllBoards(pageable);

		//then
		assertEquals(5, boards.size());
	}

	@Test
	@DisplayName("글 제목 수정")
	void test4(){
		//given
		Board board = Board.builder()
				.title("제목입니다1")
				.content("내용입니다1")
				.build();

		boardRepository.save(board);

		BoardEdit boardEdit = BoardEdit.builder()
			.title("제목입니다2")
			.build();

		//when
		boardService.edit(board.getId(), boardEdit);
		//then
		Board changeBoard = boardRepository.findById(board.getId())
			.orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id="+ board.getId()));

		assertEquals("제목입니다2", changeBoard.getTitle());
		assertEquals("내용입니다1", changeBoard.getContent());
	}

	@Test
	@DisplayName("글 내용 수정")
	void test5(){
		//given
		Board board = Board.builder()
			.title("제목입니다1")
			.content("내용입니다1")
			.build();

		boardRepository.save(board);

		BoardEdit boardEdit = BoardEdit.builder()
			.content("내용입니다2")
			.build();

		//when
		boardService.edit(board.getId(), boardEdit);
		//then
		Board changeBoard = boardRepository.findById(board.getId())
			.orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id="+ board.getId()));

		assertEquals("제목입니다1", changeBoard.getTitle());
		assertEquals("내용입니다2", changeBoard.getContent());
	}

	@Test
	@DisplayName("게시글 삭제")
	void test6(){
		Board board = Board.builder()
			.title("제목입니다1")
			.content("내용입니다1")
			.build();
		boardRepository.save(board);
		//when
		boardService.delete(board.getId());

		//then
		assertEquals(0, boardRepository.count());
	}



}