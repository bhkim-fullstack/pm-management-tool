package com.platformerz.pmtool.web;

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
class WorkspaceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WorkspaceRepository workspaceRepository;

	@Test
	void list_includesDefaultWorkspaceSeededByMigration() throws Exception {
		mockMvc.perform(get("/api/workspaces"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].name").value("기본"));
	}

	@Test
	void create_addsNewWorkspace() throws Exception {
		long before = workspaceRepository.count();

		mockMvc.perform(post("/api/workspaces")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new WorkspaceController.CreateWorkspaceRequest("새 워크스페이스"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("새 워크스페이스"));

		mockMvc.perform(get("/api/workspaces"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(before + 1));
	}

}
