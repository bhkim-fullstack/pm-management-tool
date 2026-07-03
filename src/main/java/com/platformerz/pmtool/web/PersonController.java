package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Person;
import com.platformerz.pmtool.repository.PersonRepository;
import com.platformerz.pmtool.repository.ProjectRepository;
import com.platformerz.pmtool.repository.TaskRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects/{projectId}/people")
public class PersonController {

	private final PersonRepository personRepository;
	private final TaskRepository taskRepository;
	private final ProjectRepository projectRepository;

	public PersonController(PersonRepository personRepository, TaskRepository taskRepository,
			ProjectRepository projectRepository) {
		this.personRepository = personRepository;
		this.taskRepository = taskRepository;
		this.projectRepository = projectRepository;
	}

	@GetMapping
	public List<PersonResponse> list(@PathVariable Long projectId) {
		Set<Long> peopleWithTasks = taskRepository.findByProjectId(projectId).stream()
			.flatMap(task -> task.getPeople().stream())
			.map(Person::getId)
			.collect(Collectors.toSet());

		return personRepository.findByProjectId(projectId).stream()
			.map(person -> PersonResponse.from(person, peopleWithTasks.contains(person.getId())))
			.toList();
	}

	@PostMapping
	public PersonResponse create(@PathVariable Long projectId, @RequestBody CreatePersonRequest request) {
		int colorIndex = (int) personRepository.countByProjectId(projectId);
		Person person = new Person(projectRepository.getReferenceById(projectId), request.name(), colorIndex);
		return PersonResponse.from(personRepository.save(person), false);
	}

	@DeleteMapping("/{personId}")
	public void delete(@PathVariable Long personId) {
		personRepository.deleteById(personId);
	}

	public record CreatePersonRequest(String name) {
	}

	public record PersonResponse(Long id, String name, int colorIndex, boolean hasTasks) {
		static PersonResponse from(Person person, boolean hasTasks) {
			return new PersonResponse(person.getId(), person.getName(), person.getColorIndex(), hasTasks);
		}
	}

}
