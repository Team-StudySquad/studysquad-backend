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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studysquad.docs.util.TokenFactory;
import com.studysquad.global.security.Token;
import com.studysquad.mission.controller.MissionController;
import com.studysquad.mission.domain.MissionStatus;
import com.studysquad.mission.dto.MissionCreateDto;
import com.studysquad.mission.dto.MissionEditDto;
import com.studysquad.mission.dto.MissionResponseDto;
import com.studysquad.mission.service.MissionService;
import com.studysquad.user.dto.LoginUser;

@WebMvcTest(MissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class MissionDocumentationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private MissionService missionService;

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("진행중인 미션 조회")
	void successGetProcessMission() throws Exception {
		Long squadId = 1L;

		Token token = TokenFactory.createToken();

		MissionResponseDto responseDto = MissionResponseDto.builder()
			.missionId(1L)
			.missionTitle("mission1")
			.missionContent("미션 내용 입니다")
			.missionSequence(0)
			.missionStatus(MissionStatus.END)
			.build();

		when(missionService.getProcessMission(any(Long.class), any(LoginUser.class)))
			.thenReturn(responseDto);

		mockMvc.perform(get("/api/squad/{squadId}/mission/process", squadId)
				.accept(MediaType.APPLICATION_JSON)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData()))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-process-mission",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data.missionId").description("미션 아이디"),
					fieldWithPath("data.missionTitle").description("미션 제목"),
					fieldWithPath("data.missionContent").description("미션 내용"),
					fieldWithPath("data.missionSequence").description("미션 순서"),
					fieldWithPath("data.missionStatus").description("미션 상태")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("미션 리스트 조회")
	void successGetMissions() throws Exception {
		Long squadId = 1L;

		Token token = TokenFactory.createToken();

		List<MissionResponseDto> responseData = LongStream.range(0, 3)
			.mapToObj(i -> MissionResponseDto.builder()
				.missionId(i)
				.missionTitle("mission" + i)
				.missionContent("미션 설명글 입니다" + i)
				.missionSequence((int)i)
				.missionStatus(i == 0 ? MissionStatus.PROCESS : MissionStatus.NOT_PROCESS)
				.build())
			.collect(Collectors.toList());

		when(missionService.getMissions(any(Long.class), any(LoginUser.class)))
			.thenReturn(responseData);

		mockMvc.perform(get("/api/squad/{squadId}/missions", squadId)
				.accept(MediaType.APPLICATION_JSON)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData()))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("get-missions",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지"),
					fieldWithPath("data[].missionId").description("미션 아이디"),
					fieldWithPath("data[].missionTitle").description("미션 제목"),
					fieldWithPath("data[].missionContent").description("미션 내용"),
					fieldWithPath("data[].missionSequence").description("미션 순서"),
					fieldWithPath("data[].missionStatus").description("미션 상태")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("미션 생성")
	void successCreateMission() throws Exception {
		Long squadId = 1L;

		Token token = TokenFactory.createToken();

		List<MissionCreateDto> createRequest = LongStream.range(0, 3)
			.mapToObj(i -> MissionCreateDto.builder()
				.missionTitle("미션 제목" + i)
				.missionContent("미션 내용 입니다" + i)
				.missionSequence((int)i)
				.build())
			.collect(Collectors.toList());

		String json = objectMapper.writeValueAsString(createRequest);

		mockMvc.perform(post("/api/squad/{squadId}/mission", squadId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andDo(print())
			.andDo(document("create-mission",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디")
				),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				requestFields(
					fieldWithPath("[].missionTitle").description("미션 제목"),
					fieldWithPath("[].missionContent").description("미션 내용"),
					fieldWithPath("[].missionSequence").description("미션 순서")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}

	@Test
	@WithMockUser(username = "aaa@aaa.com", roles = "USER")
	@DisplayName("미션 수정")
	void successEditMission() throws Exception {
		Long squadId = 1L;
		Long missionId = 1L;

		Token token = TokenFactory.createToken();

		MissionEditDto editRequest = MissionEditDto.builder()
			.missionTitle("수정된 미션 제목")
			.missionContent("수정된 미션 내용입니다")
			.build();

		String json = objectMapper.writeValueAsString(editRequest);

		mockMvc.perform(patch("/api/squad/{squadId}/mission/{missionId}", squadId, missionId)
				.header(token.getAccessToken().getHeader(), token.getAccessToken().getData())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("edit-mission",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("squadId").description("스쿼드 아이디"),
					parameterWithName("missionId").description("미션 아이디")
				),
				requestHeaders(
					headerWithName("Authorization").description("어세스 토큰")
				),
				requestFields(
					fieldWithPath("missionTitle").description("미션 제목"),
					fieldWithPath("missionContent").description("미션 내용")
				),
				responseFields(
					fieldWithPath("status").description("상태 코드"),
					fieldWithPath("message").description("상태 메세지")
				)));
	}
}
