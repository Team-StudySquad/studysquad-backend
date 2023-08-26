package com.studysquad.docs;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.docs.util.TokenFactory;
import com.studysquad.global.security.Token;
import com.studysquad.squad.controller.SquadController;
import com.studysquad.squad.domain.SquadStatus;
import com.studysquad.squad.dto.EndSquadDto;
import com.studysquad.squad.dto.ProcessSquadDto;
import com.studysquad.squad.dto.SquadCreateDto;
import com.studysquad.squad.dto.SquadJoinDto;
import com.studysquad.squad.dto.SquadResponseDto;
import com.studysquad.squad.dto.SquadSearchCondition;
import com.studysquad.squad.dto.UserSquadResponseDto;
import com.studysquad.squad.service.SquadService;
import com.studysquad.user.dto.LoginUser;

@WebMvcTest(SquadController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class SquadDocumentationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private SquadService squadService;

	@Test
	@DisplayName("스쿼드 단건 조회")
	void successGetSquad() throws Exception {
		Long squadId = 1L;

		SquadResponseDto responseDto = SquadResponseDto.builder()
			.categoryName("JAVA")
			.squadId(squadId)
			.squadName("squad")
			.squadExplain("squadExplain")
			.build();

		when(squadService.getSquad(any(Long.class)))
			.thenReturn(responseDto);

		mockMvc.perform(get("/api/squad/{squadId}", squadId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("squad-get",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data.categoryName").description("카테고리 이름"),
					fieldWithPath("data.squadId").description("스쿼드 아이디"),
					fieldWithPath("data.squadName").description("스쿼드 이름"),
					fieldWithPath("data.squadExplain").description("스쿼드 설명")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("진행중인 스쿼드 조회")
	void successGetProcessSquad() throws Exception {
		Token token = TokenFactory.createToken();

		ProcessSquadDto responseDto = ProcessSquadDto.builder()
			.squadId(1L)
			.categoryName("JAVA")
			.squadName("도란도란")
			.squadExplain("자바를 공부하는 스쿼드 입니다!")
			.build();

		when(squadService.getProcessSquad(any(LoginUser.class)))
			.thenReturn(responseDto);

		mockMvc.perform(get("/api/squad/process")
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData()))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-process-squad",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data.squadId").description("스쿼드 아이디"),
					fieldWithPath("data.categoryName").description("카테고리 이름"),
					fieldWithPath("data.squadName").description("스쿼드 이름"),
					fieldWithPath("data.squadExplain").description("스쿼드 설명")
				)));
	}

	@Test
	@DisplayName("모집중인 스쿼드 조회")
	void successGetRecruitSquads() throws Exception {
		PageRequest page = PageRequest.of(0, 10);
		List<SquadResponseDto> responseData = LongStream.range(1, 3)
			.mapToObj(i -> SquadResponseDto.builder()
				.squadId(i)
				.userCount(3L)
				.categoryName("JAVA")
				.squadName("squad" + i)
				.squadExplain("스쿼드 설명글 입니다" + i)
				.creatorName("user" + i)
				.build())
			.collect(Collectors.toList());

		Page<SquadResponseDto> responseDto = new PageImpl<>(responseData, page, responseData.size());
		SquadSearchCondition cond = SquadSearchCondition.builder()
			.mentor(true)
			.categoryName("JAVA")
			.build();

		when(squadService.getRecruitSquads(any(SquadSearchCondition.class), any(Pageable.class)))
			.thenReturn(responseDto);

		mockMvc.perform(get("/api/squad/recruit")
				.accept(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page.getPageSize()))
				.param("size", String.valueOf(page.getPageNumber()))
				.param("mentor", String.valueOf(cond.getMentor()))
				.param("categoryName", cond.getCategoryName()))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-recruit-squad",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					parameterWithName("page").description("페이지 번호"),
					parameterWithName("size").description("페이지 사이즈"),
					parameterWithName("mentor").description("멘토 여부"),
					parameterWithName("categoryName").description("카테고리 이름")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data.content[].squadId").description("스쿼드 아이디"),
					fieldWithPath("data.content[].userCount").description("스쿼드 인원"),
					fieldWithPath("data.content[].categoryName").description("카테고리 이름"),
					fieldWithPath("data.content[].squadName").description("스쿼드 이름"),
					fieldWithPath("data.content[].squadExplain").description("스쿼드 설명"),
					fieldWithPath("data.content[].creatorName").description("스쿼드 생성자 이름"),
					fieldWithPath("data.pageable.sort.empty").description("정렬 존재 여부"),
					fieldWithPath("data.pageable.sort.sorted").description("정렬 여부"),
					fieldWithPath("data.pageable.sort.unsorted").description("정렬이 안되어 있는지 여부"),
					fieldWithPath("data.pageable.offset").description("페이지 오프셋"),
					fieldWithPath("data.pageable.pageNumber").description("페이지 번호"),
					fieldWithPath("data.pageable.pageSize").description("페이지 사이즈"),
					fieldWithPath("data.pageable.paged").description("페이징 된 결과인지 여부"),
					fieldWithPath("data.pageable.unpaged").description("페이징 하지 않은 결과인지 여부"),
					fieldWithPath("data.last").description("마지막 페이지 여부"),
					fieldWithPath("data.totalPages").description("전체 페이지 수"),
					fieldWithPath("data.totalElements").description("전체 요소 수"),
					fieldWithPath("data.size").description("한 페이지당 표시할 항목의 개수"),
					fieldWithPath("data.number").description("현재 페이지 번호"),
					fieldWithPath("data.sort.empty").description("정렬 존재 여부"),
					fieldWithPath("data.sort.sorted").description("정렬 여부"),
					fieldWithPath("data.sort.unsorted").description("정렬이 안되어 있는지 여부"),
					fieldWithPath("data.numberOfElements").description("현재 페이지에 포함된 요소"),
					fieldWithPath("data.first").description("첫 페이지 여부"),
					fieldWithPath("data.empty").description("데이터 존재 여부")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("사용자 스쿼드 조회")
	void successGetUserSquads() throws Exception {
		Token token = TokenFactory.createToken();

		PageRequest page = PageRequest.of(0, 10);
		List<UserSquadResponseDto> responseData = LongStream.range(1, 3)
			.mapToObj(i -> UserSquadResponseDto.builder()
				.squadId(i)
				.categoryName("JAVA")
				.squadName("squadName" + i)
				.squadExplain("스쿼드 설명 글입니다" + i)
				.squadStatus(SquadStatus.END)
				.build())
			.collect(Collectors.toList());

		Page<UserSquadResponseDto> responseDto = new PageImpl<>(responseData, page, responseData.size());

		when(squadService.getUserSquads(any(LoginUser.class), any(Pageable.class)))
			.thenReturn(responseDto);

		mockMvc.perform(get("/api/squads")
				.accept(MediaType.APPLICATION_JSON)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.param("page", String.valueOf(page.getPageNumber()))
				.param("size", String.valueOf(page.getPageSize())))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-user-squad",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				requestParameters(
					parameterWithName("page").description("페이지 번호"),
					parameterWithName("size").description("페이지 크기")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data.content[].squadId").description("스쿼드 아이디"),
					fieldWithPath("data.content[].categoryName").description("카테고리 이름"),
					fieldWithPath("data.content[].squadName").description("스쿼드 이름"),
					fieldWithPath("data.content[].squadExplain").description("스쿼드 설명"),
					fieldWithPath("data.content[].squadStatus").description("스쿼드 상태"),
					fieldWithPath("data.pageable.sort.empty").description("정렬 존재 여부"),
					fieldWithPath("data.pageable.sort.sorted").description("정렬 여부"),
					fieldWithPath("data.pageable.sort.unsorted").description("정렬이 안되어 있는지 여부"),
					fieldWithPath("data.pageable.offset").description("페이지 오프셋"),
					fieldWithPath("data.pageable.pageNumber").description("페이지 번호"),
					fieldWithPath("data.pageable.pageSize").description("페이지 사이즈"),
					fieldWithPath("data.pageable.paged").description("페이징 된 결과인지 여부"),
					fieldWithPath("data.pageable.unpaged").description("페이징 하지 않은 결과인지 여부"),
					fieldWithPath("data.last").description("마지막 페이지 여부"),
					fieldWithPath("data.totalPages").description("전체 페이지 수"),
					fieldWithPath("data.totalElements").description("전체 요소 수"),
					fieldWithPath("data.size").description("한 페이지당 표시할 항목의 개수"),
					fieldWithPath("data.number").description("현재 페이지 번호"),
					fieldWithPath("data.sort.empty").description("정렬 존재 여부"),
					fieldWithPath("data.sort.sorted").description("정렬 여부"),
					fieldWithPath("data.sort.unsorted").description("정렬이 안되어 있는지 여부"),
					fieldWithPath("data.numberOfElements").description("현재 페이지에 포함된 요소"),
					fieldWithPath("data.first").description("첫 페이지 여부"),
					fieldWithPath("data.empty").description("데이터 존재 여부")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("종료된 스쿼드 단건 조회")
	void successGetEndSquad() throws Exception {
		Long squadId = 1L;

		Token token = TokenFactory.createToken();

		EndSquadDto responseDto = EndSquadDto.builder()
			.squadId(squadId)
			.squadName("도란도란")
			.categoryName("JAVA")
			.squadExplain("자바를 공부하는 스쿼드 입니다!")
			.build();

		when(squadService.getEndSquad(any(Long.class), any(LoginUser.class)))
			.thenReturn(responseDto);

		mockMvc.perform(get("/api/squad/end/{squadId}", squadId)
				.accept(MediaType.APPLICATION_JSON)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData()))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-end-squad",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디")
				),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data.squadId").description("스쿼드 아이디"),
					fieldWithPath("data.categoryName").description("카테고리 이름"),
					fieldWithPath("data.squadName").description("스쿼드 이름"),
					fieldWithPath("data.squadExplain").description("스쿼드 설명")
				)));
	}

	@Test
	@DisplayName("스쿼드 생성")
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	void successCreateSquad() throws Exception {
		Token token = TokenFactory.createToken();

		SquadCreateDto createRequest = SquadCreateDto.builder()
			.categoryName("JAVA")
			.squadName("squad")
			.squadExplain("squadExplain")
			.mentor(true)
			.build();

		String json = objectMapper.writeValueAsString(createRequest);

		mockMvc.perform(post("/api/squad")
				.contentType(MediaType.APPLICATION_JSON)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.content(json))
			.andExpect(status().isCreated())
			.andDo(print())
			.andDo(document("squad-create",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				requestFields(
					fieldWithPath("categoryName").description("카테고리 이름"),
					fieldWithPath("squadName").description("스쿼드 이름"),
					fieldWithPath("squadExplain").description("스쿼드 설명"),
					fieldWithPath("mentor").description("멘토 여부")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

	@Test
	@DisplayName("스쿼드 가입")
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	void successJoinSquad() throws Exception {
		Long squadId = 1L;
		Token token = TokenFactory.createToken();

		SquadJoinDto joinDto = SquadJoinDto.builder()
			.mentor(false)
			.build();

		String json = objectMapper.writeValueAsString(joinDto);

		mockMvc.perform(post("/api/squad/{squadId}/join", squadId)
				.contentType(MediaType.APPLICATION_JSON)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.content(json))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("squad-join",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				requestFields(
					fieldWithPath("mentor").description("멘토 여부")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}
}
