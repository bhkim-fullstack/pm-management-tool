package com.platformerz.pmtool.web;

import tools.jackson.databind.ObjectMapper;
import com.platformerz.pmtool.domain.Person;
import com.platformerz.pmtool.domain.Project;
import com.platformerz.pmtool.domain.Task;
import com.platformerz.pmtool.domain.Workspace;
import com.platformerz.pmtool.repository.PersonRepository;
import com.platformerz.pmtool.repository.ProjectRepository;
import com.platformerz.pmtool.repository.TaskRepository;
import com.platformerz.pmtool.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private WorkspaceRepository workspaceRepository;

	@Test
	void list_returnsTasksWithExclusiveEndDateForFullCalendar() throws Exception {
		Workspace workspace = workspaceRepository.save(new Workspace("워크스페이스"));
		Project project = projectRepository.save(new Project(workspace, "프로젝트", "#0969da"));
		Person person = personRepository.save(new Person(project, "담당자", 0));
		Task task = taskRepository.save(new Task(project, new ArrayList<>(List.of(person)), "할 일",
			LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3)));

		mockMvc.perform(get("/api/projects/{projectId}/tasks", project.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(task.getId()))
			.andExpect(jsonPath("$[0].title").value("할 일"))
			.andExpect(jsonPath("$[0].start").value("2026-07-01"))
			.andExpect(jsonPath("$[0].end").value("2026-07-04"))
			.andExpect(jsonPath("$[0].personIds[0]").value(person.getId()));
	}

	@Test
	void create_savesTaskAndReturnsExclusiveEndDate() throws Exception {
		Workspace workspace = workspaceRepository.save(new Workspace("워크스페이스"));
		Project project = projectRepository.save(new Project(workspace, "프로젝트", "#0969da"));
		Person person = personRepository.save(new Person(project, "담당자", 0));
		TaskRequest request = new TaskRequest("새 일정", List.of(person.getId()),
			LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2));

		mockMvc.perform(post("/api/projects/{projectId}/tasks", project.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("새 일정"))
			.andExpect(jsonPath("$.start").value("2026-08-01"))
			.andExpect(jsonPath("$.end").value("2026-08-03"))
			.andExpect(jsonPath("$.personIds[0]").value(person.getId()));

		assertThat(taskRepository.findByProjectId(project.getId())).hasSize(1);
	}

	@Test
	void create_supportsMultiplePeopleOnOneTask() throws Exception {
		Workspace workspace = workspaceRepository.save(new Workspace("워크스페이스"));
		Project project = projectRepository.save(new Project(workspace, "프로젝트", "#0969da"));
		Person personA = personRepository.save(new Person(project, "담당자A", 0));
		Person personB = personRepository.save(new Person(project, "담당자B", 1));
		TaskRequest request = new TaskRequest("공동 작업", List.of(personA.getId(), personB.getId()),
			LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2));

		mockMvc.perform(post("/api/projects/{projectId}/tasks", project.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.personIds.length()").value(2))
			.andExpect(jsonPath("$.personIds[0]").value(personA.getId()))
			.andExpect(jsonPath("$.personIds[1]").value(personB.getId()));
	}

	@Test
	void update_changesTaskFields() throws Exception {
		Workspace workspace = workspaceRepository.save(new Workspace("워크스페이스"));
		Project project = projectRepository.save(new Project(workspace, "프로젝트", "#0969da"));
		Person person = personRepository.save(new Person(project, "담당자", 0));
		Task task = taskRepository.save(new Task(project, new ArrayList<>(),"원래 제목",
			LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1)));

		TaskRequest request = new TaskRequest("수정된 제목", List.of(person.getId()),
			LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 6));

		mockMvc.perform(put("/api/tasks/{taskId}", task.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("수정된 제목"))
			.andExpect(jsonPath("$.start").value("2026-07-05"))
			.andExpect(jsonPath("$.end").value("2026-07-07"))
			.andExpect(jsonPath("$.personIds[0]").value(person.getId()));
	}

	@Test
	void delete_removesTask() throws Exception {
		Workspace workspace = workspaceRepository.save(new Workspace("워크스페이스"));
		Project project = projectRepository.save(new Project(workspace, "프로젝트", "#0969da"));
		Task task = taskRepository.save(new Task(project, new ArrayList<>(),"삭제될 일정",
			LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1)));

		mockMvc.perform(delete("/api/tasks/{taskId}", task.getId()))
			.andExpect(status().isOk());

		assertThat(taskRepository.findById(task.getId())).isEmpty();
	}

}
