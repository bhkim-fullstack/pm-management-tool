package com.platformerz.pmtool.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GlobalMemoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void get_returnsEmptyContentWhenNoMemoSavedYet() throws Exception {
		mockMvc.perform(get("/api/global-memo"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value(""));
	}

	@Test
	void update_createsThenUpdatesMemoContent() throws Exception {
		mockMvc.perform(put("/api/global-memo")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new MemoRequest("목표: 1분기 출시"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value("목표: 1분기 출시"));

		mockMvc.perform(get("/api/global-memo"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value("목표: 1분기 출시"));

		mockMvc.perform(put("/api/global-memo")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new MemoRequest("수정된 목표"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value("수정된 목표"));
	}

}
