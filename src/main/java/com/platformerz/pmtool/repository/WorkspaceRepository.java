package com.platformerz.pmtool.repository;

import com.platformerz.pmtool.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

	List<Workspace> findAllByOrderByIdAsc();

}
