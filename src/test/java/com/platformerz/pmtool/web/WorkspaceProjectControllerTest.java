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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkspaceProjectControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private WorkspaceRepository workspaceRepository;

	@Test
	void create_assignsNextDefaultColorWithinWorkspace() throws Exception {
		Workspace workspace = workspaceRepository.save(new Workspace("워크스페이스"));
		projectRepository.save(new Project(workspace, "기존 프로젝트", "#0969da"));

		mockMvc.perform(post("/api/workspaces/{workspaceId}/projects", workspace.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new WorkspaceProjectController.CreateProjectRequest("새 프로젝트"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("새 프로젝트"))
			.andExpect(jsonPath("$.color").value("#8250df"));
	}

	@Test
	void list_returnsOnlyProjectsBelongingToTheWorkspace() throws Exception {
		Workspace workspaceA = workspaceRepository.save(new Workspace("워크스페이스 A"));
		Workspace workspaceB = workspaceRepository.save(new Workspace("워크스페이스 B"));
		Project projectInA = projectRepository.save(new Project(workspaceA, "A 프로젝트", "#0969da"));
		projectRepository.save(new Project(workspaceB, "B 프로젝트", "#0969da"));

		mockMvc.perform(get("/api/workspaces/{workspaceId}/projects", workspaceA.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(projectInA.getId()));
	}

}
