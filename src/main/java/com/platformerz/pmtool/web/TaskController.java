package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Person;
import com.platformerz.pmtool.domain.Project;
import com.platformerz.pmtool.domain.Task;
import com.platformerz.pmtool.repository.PersonRepository;
import com.platformerz.pmtool.repository.ProjectRepository;
import com.platformerz.pmtool.repository.TaskRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

	private final TaskRepository taskRepository;
	private final PersonRepository personRepository;
	private final ProjectRepository projectRepository;

	public TaskController(TaskRepository taskRepository, PersonRepository personRepository,
			ProjectRepository projectRepository) {
		this.taskRepository = taskRepository;
		this.personRepository = personRepository;
		this.projectRepository = projectRepository;
	}

	@GetMapping
	public List<TaskResponse> list(@PathVariable Long projectId) {
		return taskRepository.findByProjectId(projectId).stream()
			.map(TaskResponse::from)
			.toList();
	}

	@PostMapping
	public TaskResponse create(@PathVariable Long projectId, @RequestBody TaskRequest request) {
		Project project = projectRepository.getReferenceById(projectId);
		Person person = request.personId() == null ? null : personRepository.getReferenceById(request.personId());
		Task task = new Task(project, person, request.title(), request.start(), request.end());
		return TaskResponse.from(taskRepository.save(task));
	}

}
