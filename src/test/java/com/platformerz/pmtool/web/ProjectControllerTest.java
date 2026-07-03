package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Project;
import com.platformerz.pmtool.domain.Workspace;
import com.platformerz.pmtool.repository.ProjectRepository;
import com.platformerz.pmtool.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private WorkspaceRepository workspaceRepository;

	@Test
	void updateColor_changesProjectColor() throws Exception {
		Workspace workspace = workspaceRepository.save(new Workspace("워크스페이스"));
		Project project = projectRepository.save(new Project(workspace, "프로젝트", "#0969da"));

		mockMvc.perform(put("/api/projects/{projectId}/color", project.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new ProjectController.UpdateColorRequest("#ff6600"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.color").value("#ff6600"));
	}

}
