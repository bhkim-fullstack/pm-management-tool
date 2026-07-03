package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Project;
import com.platformerz.pmtool.repository.ProjectRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

	private final ProjectRepository projectRepository;

	public ProjectController(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@PutMapping("/{projectId}/color")
	public ProjectResponse updateColor(@PathVariable Long projectId, @RequestBody UpdateColorRequest request) {
		Project project = projectRepository.findById(projectId).orElseThrow();
		project.setColor(request.color());
		return ProjectResponse.from(projectRepository.save(project));
	}

	public record UpdateColorRequest(String color) {
	}

	public record ProjectResponse(Long id, String name, String color) {
		static ProjectResponse from(Project project) {
			return new ProjectResponse(project.getId(), project.getName(), project.getColor());
		}
	}

}
