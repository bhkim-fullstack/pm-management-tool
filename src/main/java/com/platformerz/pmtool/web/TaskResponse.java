package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Person;
import com.platformerz.pmtool.domain.Task;

import java.time.LocalDate;

public record TaskResponse(Long id, String title, LocalDate start, LocalDate end, Long personId) {

	// FullCalendar treats the "end" of an all-day event as exclusive.
	public static TaskResponse from(Task task) {
		Person person = task.getPerson();
		return new TaskResponse(task.getId(), task.getTitle(), task.getStartDate(),
			task.getEndDate().plusDays(1), person == null ? null : person.getId());
	}

}
