package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Person;
import com.platformerz.pmtool.domain.Task;

import java.time.LocalDate;
import java.util.List;

public record TaskResponse(Long id, String title, LocalDate start, LocalDate end, List<Long> personIds) {

	// FullCalendar treats the "end" of an all-day event as exclusive.
	public static TaskResponse from(Task task) {
		return new TaskResponse(task.getId(), task.getTitle(), task.getStartDate(),
			task.getEndDate().plusDays(1), task.getPeople().stream().map(Person::getId).toList());
	}

}
