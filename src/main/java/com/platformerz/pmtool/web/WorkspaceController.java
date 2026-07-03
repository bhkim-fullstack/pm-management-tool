package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Workspace;
import com.platformerz.pmtool.repository.WorkspaceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

	private final WorkspaceRepository workspaceRepository;

	public WorkspaceController(WorkspaceRepository workspaceRepository) {
		this.workspaceRepository = workspaceRepository;
	}

	@GetMapping
	public List<WorkspaceResponse> list() {
		return workspaceRepository.findAllByOrderByIdAsc().stream()
			.map(WorkspaceResponse::from)
			.toList();
	}

	@PostMapping
	public WorkspaceResponse create(@RequestBody CreateWorkspaceRequest request) {
		Workspace workspace = new Workspace(request.name());
		return WorkspaceResponse.from(workspaceRepository.save(workspace));
	}

	public record CreateWorkspaceRequest(String name) {
	}

	public record WorkspaceResponse(Long id, String name) {
		static WorkspaceResponse from(Workspace workspace) {
			return new WorkspaceResponse(workspace.getId(), workspace.getName());
		}
	}

}
