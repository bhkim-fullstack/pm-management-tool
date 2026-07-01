package com.platformerz.pmtool.repository;

import com.platformerz.pmtool.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
