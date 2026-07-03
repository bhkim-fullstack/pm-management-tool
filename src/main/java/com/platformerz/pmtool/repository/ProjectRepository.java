package com.platformerz.pmtool.repository;

import com.platformerz.pmtool.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

	List<Project> findByWorkspaceId(Long workspaceId);

	long countByWorkspaceId(Long workspaceId);

}
