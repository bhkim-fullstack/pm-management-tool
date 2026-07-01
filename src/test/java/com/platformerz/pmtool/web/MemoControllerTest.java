package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Project;
import com.platformerz.pmtool.repository.ProjectRepository;
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
class MemoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@Test
	void get_returnsEmptyContentWhenNoMemoSavedYet() throws Exception {
		Project project = projectRepository.save(new Project("프로젝트"));

		mockMvc.perform(get("/api/projects/{projectId}/memo", project.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value(""));
	}

	@Test
	void update_createsThenUpdatesMemoContent() throws Exception {
		Project project = projectRepository.save(new Project("프로젝트"));

		mockMvc.perform(put("/api/projects/{projectId}/memo", project.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new MemoController.MemoRequest("첫 메모"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value("첫 메모"));

		mockMvc.perform(get("/api/projects/{projectId}/memo", project.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value("첫 메모"));

		mockMvc.perform(put("/api/projects/{projectId}/memo", project.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new MemoController.MemoRequest("수정된 메모"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value("수정된 메모"));
	}

}
