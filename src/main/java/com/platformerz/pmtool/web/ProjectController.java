package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Project;
import com.platformerz.pmtool.repository.ProjectRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

	private final ProjectRepository projectRepository;

	public ProjectController(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@GetMapping
	public List<ProjectResponse> list() {
		return projectRepository.findAll().stream()
			.map(ProjectResponse::from)
			.toList();
	}

	@PostMapping
	public ProjectResponse create(@RequestBody CreateProjectRequest request) {
		Project project = new Project(request.name());
		return ProjectResponse.from(projectRepository.save(project));
	}

	public record CreateProjectRequest(String name) {
	}

	public record ProjectResponse(Long id, String name) {
		static ProjectResponse from(Project project) {
			return new ProjectResponse(project.getId(), project.getName());
		}
	}

}
