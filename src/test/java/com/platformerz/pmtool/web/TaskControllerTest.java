package com.platformerz.pmtool.web;

import tools.jackson.databind.ObjectMapper;
import com.platformerz.pmtool.domain.Person;
import com.platformerz.pmtool.domain.Project;
import com.platformerz.pmtool.domain.Task;
import com.platformerz.pmtool.repository.PersonRepository;
import com.platformerz.pmtool.repository.ProjectRepository;
import com.platformerz.pmtool.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

	@Test
	void list_returnsTasksWithExclusiveEndDateForFullCalendar() throws Exception {
		Project project = projectRepository.save(new Project("프로젝트"));
		Person person = personRepository.save(new Person(project, "담당자", 0));
		Task task = taskRepository.save(new Task(project, person, "할 일",
			LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3)));

		mockMvc.perform(get("/api/projects/{projectId}/tasks", project.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(task.getId()))
			.andExpect(jsonPath("$[0].title").value("할 일"))
			.andExpect(jsonPath("$[0].start").value("2026-07-01"))
			.andExpect(jsonPath("$[0].end").value("2026-07-04"))
			.andExpect(jsonPath("$[0].personId").value(person.getId()));
	}

	@Test
	void create_savesTaskAndReturnsExclusiveEndDate() throws Exception {
		Project project = projectRepository.save(new Project("프로젝트"));
		Person person = personRepository.save(new Person(project, "담당자", 0));
		TaskRequest request = new TaskRequest("새 일정", person.getId(),
			LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2));

		mockMvc.perform(post("/api/projects/{projectId}/tasks", project.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("새 일정"))
			.andExpect(jsonPath("$.start").value("2026-08-01"))
			.andExpect(jsonPath("$.end").value("2026-08-03"))
			.andExpect(jsonPath("$.personId").value(person.getId()));

		assertThat(taskRepository.findByProjectId(project.getId())).hasSize(1);
	}

	@Test
	void update_changesTaskFields() throws Exception {
		Project project = projectRepository.save(new Project("프로젝트"));
		Person person = personRepository.save(new Person(project, "담당자", 0));
		Task task = taskRepository.save(new Task(project, null, "원래 제목",
			LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1)));

		TaskRequest request = new TaskRequest("수정된 제목", person.getId(),
			LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 6));

		mockMvc.perform(put("/api/tasks/{taskId}", task.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("수정된 제목"))
			.andExpect(jsonPath("$.start").value("2026-07-05"))
			.andExpect(jsonPath("$.end").value("2026-07-07"))
			.andExpect(jsonPath("$.personId").value(person.getId()));
	}

	@Test
	void delete_removesTask() throws Exception {
		Project project = projectRepository.save(new Project("프로젝트"));
		Task task = taskRepository.save(new Task(project, null, "삭제될 일정",
			LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1)));

		mockMvc.perform(delete("/api/tasks/{taskId}", task.getId()))
			.andExpect(status().isOk());

		assertThat(taskRepository.findById(task.getId())).isEmpty();
	}

}
