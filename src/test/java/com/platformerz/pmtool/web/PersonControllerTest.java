package com.platformerz.pmtool.web;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PersonControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Test
	void list_splitsPeopleByWhetherTheyHaveTasks() throws Exception {
		Project project = projectRepository.save(new Project("프로젝트"));
		Person withTask = personRepository.save(new Person(project, "태스크 있음", 0));
		Person withoutTask = personRepository.save(new Person(project, "태스크 없음", 1));
		taskRepository.save(new Task(project, withTask, "할 일", LocalDate.now(), LocalDate.now().plusDays(1)));

		mockMvc.perform(get("/api/projects/{projectId}/people", project.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.id == %d)].hasTasks".formatted(withTask.getId())).value(contains(true)))
			.andExpect(jsonPath("$[?(@.id == %d)].hasTasks".formatted(withoutTask.getId())).value(contains(false)));
	}

}
