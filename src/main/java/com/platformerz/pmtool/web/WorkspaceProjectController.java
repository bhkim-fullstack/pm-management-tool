package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Project;
import com.platformerz.pmtool.repository.ProjectRepository;
import com.platformerz.pmtool.repository.WorkspaceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects")
public class WorkspaceProjectController {

	private static final String[] DEFAULT_COLORS = {
		"#0969da", "#8250df", "#1a7f37", "#bf3989", "#d1242f", "#9a6700", "#0550ae", "#57606a"
	};

	private final ProjectRepository projectRepository;
	private final WorkspaceRepository workspaceRepository;

	public WorkspaceProjectController(ProjectRepository projectRepository, WorkspaceRepository workspaceRepository) {
		this.projectRepository = projectRepository;
		this.workspaceRepository = workspaceRepository;
	}

	@GetMapping
	public List<ProjectController.ProjectResponse> list(@PathVariable Long workspaceId) {
		return projectRepository.findByWorkspaceId(workspaceId).stream()
			.map(ProjectController.ProjectResponse::from)
			.toList();
	}

	@PostMapping
	public ProjectController.ProjectResponse create(@PathVariable Long workspaceId,
			@RequestBody CreateProjectRequest request) {
		String color = DEFAULT_COLORS[(int) (projectRepository.countByWorkspaceId(workspaceId) % DEFAULT_COLORS.length)];
		Project project = new Project(workspaceRepository.getReferenceById(workspaceId), request.name(), color);
		return ProjectController.ProjectResponse.from(projectRepository.save(project));
	}

	public record CreateProjectRequest(String name) {
	}

}
