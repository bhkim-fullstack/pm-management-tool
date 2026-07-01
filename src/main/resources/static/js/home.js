const PERSON_COLORS = ["#0969da", "#8250df", "#1a7f37", "#bf3989", "#d1242f", "#9a6700", "#0550ae", "#57606a"];
const UNASSIGNED_COLOR = "#8b949e";

let selectedProjectId = null;
let currentPeople = [];
let peopleColorIndexById = new Map();
let editingTaskId = null;

const taskModal = document.getElementById("task-modal");
const taskForm = document.getElementById("task-form");
const taskTitleInput = document.getElementById("task-title");
const taskPersonSelect = document.getElementById("task-person");
const taskStartInput = document.getElementById("task-start");
const taskEndInput = document.getElementById("task-end");
const taskModalTitle = document.getElementById("task-modal-title");
const taskDeleteButton = document.getElementById("task-delete-button");

const calendarTitle = document.getElementById("calendar-title");

const calendar = new FullCalendar.Calendar(document.getElementById("calendar"), {
	initialView: "multiMonthYear",
	multiMonthMaxColumns: 1,
	height: "auto",
	locale: "ko",
	headerToolbar: false,
	selectable: true,
	editable: true,
	events: fetchCalendarEvents,
	datesSet: (info) => {
		calendarTitle.textContent = info.view.title;
		scrollToToday();
	},
	dateClick: (info) => {
		if (selectedProjectId === null) {
			return;
		}
		openCreateModal(info.dateStr, info.dateStr);
	},
	select: (info) => {
		if (selectedProjectId === null) {
			calendar.unselect();
			return;
		}
		openCreateModal(info.startStr, addDaysToDateString(info.endStr, -1));
		calendar.unselect();
	},
	eventClick: (info) => {
		openEditModal({
			id: Number(info.event.id),
			title: info.event.title,
			personId: info.event.extendedProps.personId,
			start: info.event.startStr,
			end: addDaysToDateString(info.event.endStr, -1),
		});
	},
	eventDrop: (info) => rescheduleTask(info.event),
	eventResize: (info) => rescheduleTask(info.event),
});
calendar.render();

document.getElementById("calendar-today-button").addEventListener("click", () => {
	calendar.today();
	scrollToToday();
});
document.getElementById("calendar-prev-button").addEventListener("click", () => calendar.prev());
document.getElementById("calendar-next-button").addEventListener("click", () => calendar.next());

function scrollToToday() {
	setTimeout(() => {
		document.querySelector("#calendar .fc-day-today")?.scrollIntoView({ block: "center" });
	}, 0);
}

function addDaysToDateString(dateStr, days) {
	const date = new Date(`${dateStr}T00:00:00Z`);
	date.setUTCDate(date.getUTCDate() + days);
	return date.toISOString().slice(0, 10);
}

async function loadProjects() {
	const response = await fetch("/api/projects");
	const projects = await response.json();
	renderProjectList(projects);
	await loadPeople();
	await loadMemo();
}

function renderProjectList(projects) {
	const list = document.getElementById("project-list");
	list.innerHTML = "";

	if (selectedProjectId === null && projects.length > 0) {
		selectedProjectId = projects[0].id;
	}

	for (const project of projects) {
		const item = document.createElement("li");
		item.textContent = project.name;
		item.dataset.projectId = project.id;
		if (project.id === selectedProjectId) {
			item.classList.add("selected");
		}
		item.addEventListener("click", () => selectProject(project.id));
		list.appendChild(item);
	}
}

async function selectProject(projectId) {
	selectedProjectId = projectId;
	document.querySelectorAll("#project-list li").forEach((item) => {
		item.classList.toggle("selected", Number(item.dataset.projectId) === projectId);
	});
	await loadPeople();
	await loadMemo();
}

async function createProject(name) {
	const response = await fetch("/api/projects", {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ name }),
	});
	const project = await response.json();
	selectedProjectId = project.id;
	await loadProjects();
}

async function loadPeople() {
	const withTasksContainer = document.getElementById("people-with-tasks");
	const withoutTasksContainer = document.getElementById("people-without-tasks");

	if (selectedProjectId === null) {
		withTasksContainer.innerHTML = "";
		withoutTasksContainer.innerHTML = "";
		currentPeople = [];
		peopleColorIndexById = new Map();
		calendar.refetchEvents();
		return;
	}

	const response = await fetch(`/api/projects/${selectedProjectId}/people`);
	currentPeople = await response.json();
	renderPeople(currentPeople);
	peopleColorIndexById = new Map(currentPeople.map((person) => [person.id, person.colorIndex]));
	calendar.refetchEvents();
}

function renderPeople(people) {
	const withTasksContainer = document.getElementById("people-with-tasks");
	const withoutTasksContainer = document.getElementById("people-without-tasks");
	withTasksContainer.innerHTML = "";
	withoutTasksContainer.innerHTML = "";

	for (const person of people) {
		const container = person.hasTasks ? withTasksContainer : withoutTasksContainer;
		container.appendChild(createPersonChip(person));
	}
}

function createPersonChip(person) {
	const chip = document.createElement("span");
	chip.className = "person-chip" + (person.hasTasks ? "" : " no-task");
	if (person.hasTasks) {
		chip.style.backgroundColor = PERSON_COLORS[person.colorIndex % PERSON_COLORS.length];
	}

	const name = document.createElement("span");
	name.textContent = person.name;
	chip.appendChild(name);

	const deleteButton = document.createElement("button");
	deleteButton.type = "button";
	deleteButton.textContent = "×";
	deleteButton.addEventListener("click", () => deletePerson(person.id));
	chip.appendChild(deleteButton);

	return chip;
}

async function createPerson(name) {
	await fetch(`/api/projects/${selectedProjectId}/people`, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ name }),
	});
	await loadPeople();
}

async function deletePerson(personId) {
	await fetch(`/api/projects/${selectedProjectId}/people/${personId}`, { method: "DELETE" });
	await loadPeople();
}

async function fetchCalendarEvents(info, successCallback, failureCallback) {
	if (selectedProjectId === null) {
		successCallback([]);
		return;
	}
	try {
		const response = await fetch(`/api/projects/${selectedProjectId}/tasks`);
		const tasks = await response.json();
		successCallback(tasks.map(taskToEvent));
	} catch (error) {
		failureCallback(error);
	}
}

function taskToEvent(task) {
	const colorIndex = task.personId !== null ? peopleColorIndexById.get(task.personId) : undefined;
	const color = colorIndex !== undefined ? PERSON_COLORS[colorIndex % PERSON_COLORS.length] : UNASSIGNED_COLOR;
	return {
		id: String(task.id),
		title: task.title,
		start: task.start,
		end: task.end,
		allDay: true,
		backgroundColor: color,
		borderColor: color,
		extendedProps: { personId: task.personId },
	};
}

function populatePersonSelect() {
	taskPersonSelect.innerHTML = '<option value="">미배정</option>';
	for (const person of currentPeople) {
		const option = document.createElement("option");
		option.value = person.id;
		option.textContent = person.name;
		taskPersonSelect.appendChild(option);
	}
}

function openCreateModal(startDate, endDateInclusive) {
	editingTaskId = null;
	taskModalTitle.textContent = "새 일정";
	taskDeleteButton.hidden = true;
	populatePersonSelect();
	taskTitleInput.value = "";
	taskPersonSelect.value = "";
	taskStartInput.value = startDate;
	taskEndInput.value = endDateInclusive;
	taskModal.showModal();
	taskTitleInput.focus();
}

function openEditModal(task) {
	editingTaskId = task.id;
	taskModalTitle.textContent = "일정 수정";
	taskDeleteButton.hidden = false;
	populatePersonSelect();
	taskTitleInput.value = task.title;
	taskPersonSelect.value = task.personId ?? "";
	taskStartInput.value = task.start;
	taskEndInput.value = task.end;
	taskModal.showModal();
	taskTitleInput.focus();
}

function readTaskFormPayload() {
	return {
		title: taskTitleInput.value.trim(),
		personId: taskPersonSelect.value ? Number(taskPersonSelect.value) : null,
		start: taskStartInput.value,
		end: taskEndInput.value,
	};
}

async function createTask(payload) {
	await fetch(`/api/projects/${selectedProjectId}/tasks`, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(payload),
	});
}

async function updateTask(taskId, payload) {
	await fetch(`/api/tasks/${taskId}`, {
		method: "PUT",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(payload),
	});
}

async function deleteTask(taskId) {
	await fetch(`/api/tasks/${taskId}`, { method: "DELETE" });
}

async function rescheduleTask(event) {
	await updateTask(Number(event.id), {
		title: event.title,
		personId: event.extendedProps.personId,
		start: event.startStr,
		end: addDaysToDateString(event.endStr, -1),
	});
	calendar.refetchEvents();
}

taskForm.addEventListener("submit", async (event) => {
	event.preventDefault();
	const payload = readTaskFormPayload();
	if (!payload.title || !payload.start || !payload.end) {
		return;
	}
	if (editingTaskId === null) {
		await createTask(payload);
	} else {
		await updateTask(editingTaskId, payload);
	}
	taskModal.close();
	calendar.refetchEvents();
	await loadPeople();
});

document.getElementById("task-cancel-button").addEventListener("click", () => taskModal.close());

taskDeleteButton.addEventListener("click", async () => {
	if (editingTaskId === null) {
		return;
	}
	await deleteTask(editingTaskId);
	taskModal.close();
	calendar.refetchEvents();
	await loadPeople();
});

document.getElementById("new-project-form").addEventListener("submit", async (event) => {
	event.preventDefault();
	const input = document.getElementById("new-project-name");
	const name = input.value.trim();
	if (!name) {
		return;
	}
	await createProject(name);
	input.value = "";
});

document.getElementById("new-person-form").addEventListener("submit", async (event) => {
	event.preventDefault();
	if (selectedProjectId === null) {
		return;
	}
	const input = document.getElementById("new-person-name");
	const name = input.value.trim();
	if (!name) {
		return;
	}
	await createPerson(name);
	input.value = "";
});

const memoTextarea = document.getElementById("memo-textarea");
const memoStatus = document.getElementById("memo-status");
let memoSaveTimeout = null;
let memoLoadToken = 0;

async function loadMemo() {
	const token = ++memoLoadToken;
	if (selectedProjectId === null) {
		memoTextarea.value = "";
		memoTextarea.disabled = true;
		memoStatus.textContent = "";
		return;
	}
	const response = await fetch(`/api/projects/${selectedProjectId}/memo`);
	const memo = await response.json();
	if (token !== memoLoadToken) {
		return;
	}
	memoTextarea.disabled = false;
	memoTextarea.value = memo.content;
	memoStatus.textContent = "";
}

async function saveMemo() {
	if (selectedProjectId === null) {
		return;
	}
	memoStatus.textContent = "저장 중...";
	await fetch(`/api/projects/${selectedProjectId}/memo`, {
		method: "PUT",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ content: memoTextarea.value }),
	});
	memoStatus.textContent = "저장됨";
}

memoTextarea.addEventListener("input", () => {
	clearTimeout(memoSaveTimeout);
	memoSaveTimeout = setTimeout(saveMemo, 600);
});

loadProjects();
