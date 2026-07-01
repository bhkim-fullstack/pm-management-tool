package com.platformerz.pmtool.domain;

import com.platformerz.pmtool.domain.converter.LocalDateStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

@Entity
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@ManyToOne
	@JoinColumn(name = "person_id")
	private Person person;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	@Convert(converter = LocalDateStringConverter.class)
	private LocalDate startDate;

	@Column(nullable = false)
	@Convert(converter = LocalDateStringConverter.class)
	private LocalDate endDate;

	protected Task() {
	}

	public Task(Project project, Person person, String title, LocalDate startDate, LocalDate endDate) {
		this.project = project;
		this.person = person;
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Long getId() {
		return id;
	}

	public Project getProject() {
		return project;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

}
