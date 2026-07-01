package com.platformerz.pmtool.repository;

import com.platformerz.pmtool.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {

	List<Person> findByProjectId(Long projectId);

	long countByProjectId(Long projectId);

}
