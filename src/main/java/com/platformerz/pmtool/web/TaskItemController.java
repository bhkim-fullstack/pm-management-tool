package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Person;
import com.platformerz.pmtool.domain.Task;
import com.platformerz.pmtool.repository.PersonRepository;
import com.platformerz.pmtool.repository.TaskRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}")
public class TaskItemController {

	private final TaskRepository taskRepository;
	private final PersonRepository personRepository;

	public TaskItemController(TaskRepository taskRepository, PersonRepository personRepository) {
		this.taskRepository = taskRepository;
		this.personRepository = personRepository;
	}

	@PutMapping
	public TaskResponse update(@PathVariable Long taskId, @RequestBody TaskRequest request) {
		Task task = taskRepository.findById(taskId).orElseThrow();
		task.setTitle(request.title());
		task.setStartDate(request.start());
		task.setEndDate(request.end());
		task.setPeople(resolvePeople(request.personIds()));
		return TaskResponse.from(taskRepository.save(task));
	}

	@DeleteMapping
	public void delete(@PathVariable Long taskId) {
		taskRepository.deleteById(taskId);
	}

	private List<Person> resolvePeople(List<Long> personIds) {
		if (personIds == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(personIds.stream().map(personRepository::getReferenceById).toList());
	}

}
