const COLOR_PALETTE = ["#0969da", "#8250df", "#1a7f37", "#bf3989", "#d1242f", "#9a6700", "#0550ae", "#57606a"];
const UNASSIGNED_COLOR = "#8b949e";
const ALL_PROJECTS_ID = "all";

let selectedProjectId = null;
let currentPeople = [];
let peopleColorIndexById = new Map();
let projectColorById = new Map();
let editingTaskId = null;

let currentWorkspaces = [];
let selectedWorkspaceId = null;

function isRealProjectSelected() {
	return selectedProjectId !== null && selectedProjectId !== ALL_PROJECTS_ID;
}

const taskModal = document.getElementById("task-modal");
const taskForm = document.getElementById("task-form");
const taskTitleInput = document.getElementById("task-title");
const taskPersonListContainer = document.getElementById("task-person-list");
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
		if (!isRealProjectSelected()) {
			return;
		}
		openCreateModal(info.dateStr, info.dateStr);
	},
	select: (info) => {
		if (!isRealProjectSelected()) {
			calendar.unselect();
			return;
		}
		openCreateModal(info.startStr, addDaysToDateString(info.endStr, -1));
		calendar.unselect();
	},
	eventClick: (info) => {
		if (!isRealProjectSelected()) {
			return;
		}
		openEditModal({
			id: Number(info.event.id),
			title: info.event.title,
			personIds: info.event.extendedProps.personIds,
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

function updateCalendarEditability() {
	calendar.setOption("editable", isRealProjectSelected());
	calendar.setOption("selectable", isRealProjectSelected());
}

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

const workspaceTabsContainer = document.getElementById("workspace-tabs");
const addWorkspaceButton = document.getElementById("add-workspace-button");

async function loadWorkspaces() {
	const response = await fetch("/api/workspaces");
	currentWorkspaces = await response.json();

	if (selectedWorkspaceId === null || !currentWorkspaces.some((workspace) => workspace.id === selectedWorkspaceId)) {
		selectedWorkspaceId = currentWorkspaces[0]?.id ?? null;
	}

	renderWorkspaceTabs();
	await loadProjects();
}

function renderWorkspaceTabs() {
	workspaceTabsContainer.innerHTML = "";
	for (const workspace of currentWorkspaces) {
		const tab = document.createElement("button");
		tab.type = "button";
		tab.className = "workspace-tab" + (workspace.id === selectedWorkspaceId ? " selected" : "");
		tab.textContent = workspace.name;
		tab.addEventListener("click", () => selectWorkspace(workspace.id));
		workspaceTabsContainer.appendChild(tab);
	}
}

async function selectWorkspace(workspaceId) {
	if (workspaceId === selectedWorkspaceId) {
		return;
	}
	selectedWorkspaceId = workspaceId;
	selectedProjectId = null;
	renderWorkspaceTabs();
	await loadProjects();
}

let addWorkspacePopup = null;

function closeAddWorkspacePopup() {
	addWorkspacePopup?.remove();
	addWorkspacePopup = null;
}

addWorkspaceButton.addEventListener("click", (event) => {
	event.stopPropagation();
	if (addWorkspacePopup) {
		closeAddWorkspacePopup();
		return;
	}

	const popup = document.createElement("div");
	popup.className = "add-workspace-popup";

	const input = document.createElement("input");
	input.type = "text";
	input.placeholder = "새 워크스페이스 이름";
	popup.appendChild(input);

	const submitButton = document.createElement("button");
	submitButton.type = "button";
	submitButton.className = "btn btn-primary";
	submitButton.textContent = "추가";
	popup.appendChild(submitButton);

	const submit = async () => {
		const name = input.value.trim();
		if (!name) {
			return;
		}
		await createWorkspace(name);
		closeAddWorkspacePopup();
	};
	submitButton.addEventListener("click", (clickEvent) => {
		clickEvent.stopPropagation();
		submit();
	});
	input.addEventListener("click", (clickEvent) => clickEvent.stopPropagation());
	input.addEventListener("keydown", (keyEvent) => {
		if (keyEvent.key === "Enter") {
			keyEvent.preventDefault();
			submit();
		}
	});

	document.body.appendChild(popup);
	const anchorRect = addWorkspaceButton.getBoundingClientRect();
	popup.style.top = `${anchorRect.bottom + 4}px`;
	popup.style.left = `${anchorRect.left}px`;
	addWorkspacePopup = popup;
	input.focus();

	setTimeout(() => document.addEventListener("click", closeAddWorkspacePopup, { once: true }), 0);
});

async function createWorkspace(name) {
	const response = await fetch("/api/workspaces", {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ name }),
	});
	const workspace = await response.json();
	selectedWorkspaceId = workspace.id;
	selectedProjectId = null;
	await loadWorkspaces();
}

async function loadProjects() {
	if (selectedWorkspaceId === null) {
		renderProjectList([]);
		await loadPeople();
		await loadMemo();
		return;
	}
	const response = await fetch(`/api/workspaces/${selectedWorkspaceId}/projects`);
	const projects = await response.json();
	renderProjectList(projects);
	await loadPeople();
	await loadMemo();
}

let currentProjects = [];

const allProjectsItem = document.getElementById("all-projects-item");
allProjectsItem.addEventListener("click", () => selectProject(ALL_PROJECTS_ID));

function renderProjectList(projects) {
	currentProjects = projects;
	projectColorById = new Map(projects.map((project) => [project.id, project.color]));
	const list = document.getElementById("project-list");
	list.innerHTML = "";

	if (selectedProjectId === null && projects.length > 0) {
		selectedProjectId = projects[0].id;
	}

	allProjectsItem.classList.toggle("selected", selectedProjectId === ALL_PROJECTS_ID);

	for (const project of projects) {
		const item = document.createElement("li");
		item.dataset.projectId = project.id;
		if (project.id === selectedProjectId) {
			item.classList.add("selected");
		}

		const dot = document.createElement("span");
		dot.className = "project-color-dot";
		dot.style.backgroundColor = project.color;
		dot.addEventListener("click", (event) => {
			event.stopPropagation();
			openColorPicker(dot, project);
		});
		item.appendChild(dot);

		const name = document.createElement("span");
		name.textContent = project.name;
		item.appendChild(name);

		item.addEventListener("click", () => selectProject(project.id));
		list.appendChild(item);
	}

	updateCalendarEditability();
}

let colorPickerPopup = null;

function closeColorPicker() {
	colorPickerPopup?.remove();
	colorPickerPopup = null;
}

function openColorPicker(anchorEl, project) {
	closeColorPicker();

	const popup = document.createElement("div");
	popup.className = "color-picker-popup";

	const swatchGrid = document.createElement("div");
	swatchGrid.className = "color-picker-swatch-grid";
	for (const color of COLOR_PALETTE) {
		const swatch = document.createElement("button");
		swatch.type = "button";
		swatch.className = "color-picker-swatch";
		swatch.style.backgroundColor = color;
		swatch.addEventListener("click", async (event) => {
			event.stopPropagation();
			await updateProjectColor(project.id, color);
			closeColorPicker();
		});
		swatchGrid.appendChild(swatch);
	}
	popup.appendChild(swatchGrid);

	const customPicker = document.createElement("input");
	customPicker.type = "color";
	customPicker.className = "color-picker-custom";
	customPicker.value = project.color;
	customPicker.addEventListener("click", (event) => event.stopPropagation());
	customPicker.addEventListener("input", async (event) => {
		await updateProjectColor(project.id, event.target.value);
	});
	popup.appendChild(customPicker);

	document.body.appendChild(popup);
	const anchorRect = anchorEl.getBoundingClientRect();
	popup.style.top = `${anchorRect.bottom + 4}px`;
	popup.style.left = `${anchorRect.left}px`;
	colorPickerPopup = popup;

	setTimeout(() => document.addEventListener("click", closeColorPicker, { once: true }), 0);
}

async function updateProjectColor(projectId, color) {
	await fetch(`/api/projects/${projectId}/color`, {
		method: "PUT",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ color }),
	});
	await loadProjects();
}

async function selectProject(projectId) {
	selectedProjectId = projectId;
	allProjectsItem.classList.toggle("selected", projectId === ALL_PROJECTS_ID);
	document.querySelectorAll("#project-list li").forEach((item) => {
		item.classList.toggle("selected", item.dataset.projectId === String(projectId));
	});
	updateCalendarEditability();
	await loadPeople();
	await loadMemo();
}

async function createProject(name) {
	const response = await fetch(`/api/workspaces/${selectedWorkspaceId}/projects`, {
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
	const newPersonNameInput = document.getElementById("new-person-name");
	const newPersonSubmitButton = document.querySelector("#new-person-form button");
	newPersonNameInput.disabled = !isRealProjectSelected();
	newPersonSubmitButton.disabled = !isRealProjectSelected();

	if (selectedProjectId === null) {
		withTasksContainer.innerHTML = "";
		withoutTasksContainer.innerHTML = "";
		currentPeople = [];
		peopleColorIndexById = new Map();
		calendar.refetchEvents();
		return;
	}

	if (selectedProjectId === ALL_PROJECTS_ID) {
		const peopleLists = await Promise.all(
			currentProjects.map((project) => fetch(`/api/projects/${project.id}/people`).then((r) => r.json())),
		);
		currentPeople = peopleLists.flat();
		renderPeople(currentPeople);
		peopleColorIndexById = new Map(currentPeople.map((person) => [person.id, person.colorIndex]));
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
		chip.style.backgroundColor = COLOR_PALETTE[person.colorIndex % COLOR_PALETTE.length];
	}

	const name = document.createElement("span");
	name.textContent = person.name;
	chip.appendChild(name);

	if (isRealProjectSelected()) {
		const deleteButton = document.createElement("button");
		deleteButton.type = "button";
		deleteButton.textContent = "×";
		deleteButton.addEventListener("click", () => deletePerson(person.id));
		chip.appendChild(deleteButton);
	}

	return chip;
}

async function createPerson(name) {
	await fetch(`/api/projects/${selectedProjectId}/people`, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ name }),
	});
}

async function createPeople(names) {
	for (const name of names) {
		await createPerson(name);
	}
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
		if (selectedProjectId === ALL_PROJECTS_ID) {
			const taskLists = await Promise.all(
				currentProjects.map((project) =>
					fetch(`/api/projects/${project.id}/tasks`)
						.then((r) => r.json())
						.then((tasks) => tasks.map((task) => ({ ...task, projectId: project.id }))),
				),
			);
			successCallback(taskLists.flat().map((task) => taskToEvent(task, true)));
			return;
		}
		const response = await fetch(`/api/projects/${selectedProjectId}/tasks`);
		const tasks = await response.json();
		successCallback(tasks.map((task) => taskToEvent(task, false)));
	} catch (error) {
		failureCallback(error);
	}
}

function taskToEvent(task, colorByProject) {
	let color;
	if (colorByProject) {
		color = projectColorById.get(task.projectId);
	} else {
		const firstPersonId = task.personIds.length > 0 ? task.personIds[0] : undefined;
		const colorIndex = firstPersonId !== undefined ? peopleColorIndexById.get(firstPersonId) : undefined;
		color = colorIndex !== undefined ? COLOR_PALETTE[colorIndex % COLOR_PALETTE.length] : UNASSIGNED_COLOR;
	}
	return {
		id: String(task.id),
		title: task.title,
		start: task.start,
		end: task.end,
		allDay: true,
		backgroundColor: color,
		borderColor: color,
		extendedProps: { personIds: task.personIds },
	};
}

function populatePersonCheckboxes(selectedPersonIds) {
	taskPersonListContainer.innerHTML = "";
	if (currentPeople.length === 0) {
		const empty = document.createElement("span");
		empty.className = "task-person-empty";
		empty.textContent = "등록된 담당자가 없습니다";
		taskPersonListContainer.appendChild(empty);
		return;
	}
	for (const person of currentPeople) {
		const label = document.createElement("label");
		label.className = "task-person-option";

		const checkbox = document.createElement("input");
		checkbox.type = "checkbox";
		checkbox.value = person.id;
		checkbox.checked = selectedPersonIds.includes(person.id);
		label.appendChild(checkbox);

		const name = document.createElement("span");
		name.textContent = person.name;
		label.appendChild(name);

		taskPersonListContainer.appendChild(label);
	}
}

function openCreateModal(startDate, endDateInclusive) {
	editingTaskId = null;
	taskModalTitle.textContent = "새 일정";
	taskDeleteButton.hidden = true;
	populatePersonCheckboxes([]);
	taskTitleInput.value = "";
	taskStartInput.value = startDate;
	taskEndInput.value = endDateInclusive;
	taskModal.showModal();
	taskTitleInput.focus();
}

function openEditModal(task) {
	editingTaskId = task.id;
	taskModalTitle.textContent = "일정 수정";
	taskDeleteButton.hidden = false;
	populatePersonCheckboxes(task.personIds);
	taskTitleInput.value = task.title;
	taskStartInput.value = task.start;
	taskEndInput.value = task.end;
	taskModal.showModal();
	taskTitleInput.focus();
}

function readTaskFormPayload() {
	const personIds = Array.from(taskPersonListContainer.querySelectorAll("input:checked")).map(
		(checkbox) => Number(checkbox.value),
	);
	return {
		title: taskTitleInput.value.trim(),
		personIds,
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
		personIds: event.extendedProps.personIds,
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
	if (!isRealProjectSelected()) {
		return;
	}
	const input = document.getElementById("new-person-name");
	const names = input.value
		.split(",")
		.map((name) => name.trim())
		.filter((name) => name.length > 0);
	if (names.length === 0) {
		return;
	}
	await createPeople(names);
	input.value = "";
});

const memoTextarea = document.getElementById("memo-textarea");
const memoStatus = document.getElementById("memo-status");
const memoTabsContainer = document.getElementById("memo-tabs");
let memoSaveTimeout = null;
let memoLoadToken = 0;
let selectedMemoProjectId = null;

function activeMemoProjectId() {
	return selectedProjectId === ALL_PROJECTS_ID ? selectedMemoProjectId : selectedProjectId;
}

function renderMemoTabs() {
	memoTabsContainer.innerHTML = "";
	for (const project of currentProjects) {
		const tab = document.createElement("button");
		tab.type = "button";
		tab.className = "memo-tab" + (project.id === selectedMemoProjectId ? " selected" : "");
		tab.textContent = project.name;
		tab.addEventListener("click", () => selectMemoTab(project.id));
		memoTabsContainer.appendChild(tab);
	}
}

async function selectMemoTab(projectId) {
	selectedMemoProjectId = projectId;
	await loadMemo();
}

async function loadMemo() {
	const token = ++memoLoadToken;

	if (selectedProjectId === ALL_PROJECTS_ID) {
		memoTabsContainer.hidden = false;
		if (selectedMemoProjectId === null || !currentProjects.some((project) => project.id === selectedMemoProjectId)) {
			selectedMemoProjectId = currentProjects[0]?.id ?? null;
		}
		renderMemoTabs();
	} else {
		memoTabsContainer.hidden = true;
		selectedMemoProjectId = null;
	}

	const targetProjectId = activeMemoProjectId();
	if (targetProjectId === null) {
		memoTextarea.value = "";
		memoTextarea.disabled = true;
		memoStatus.textContent = "";
		return;
	}

	const response = await fetch(`/api/projects/${targetProjectId}/memo`);
	const memo = await response.json();
	if (token !== memoLoadToken) {
		return;
	}
	memoTextarea.disabled = false;
	memoTextarea.value = memo.content;
	memoStatus.textContent = "";
}

async function saveMemo() {
	const targetProjectId = activeMemoProjectId();
	if (targetProjectId === null) {
		return;
	}
	memoStatus.textContent = "저장 중...";
	await fetch(`/api/projects/${targetProjectId}/memo`, {
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

const globalMemoTextarea = document.getElementById("global-memo-textarea");
const globalMemoStatus = document.getElementById("global-memo-status");
let globalMemoSaveTimeout = null;

async function loadGlobalMemo() {
	const response = await fetch("/api/global-memo");
	const memo = await response.json();
	globalMemoTextarea.value = memo.content;
	globalMemoStatus.textContent = "";
}

async function saveGlobalMemo() {
	globalMemoStatus.textContent = "저장 중...";
	await fetch("/api/global-memo", {
		method: "PUT",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ content: globalMemoTextarea.value }),
	});
	globalMemoStatus.textContent = "저장됨";
}

globalMemoTextarea.addEventListener("input", () => {
	clearTimeout(globalMemoSaveTimeout);
	globalMemoSaveTimeout = setTimeout(saveGlobalMemo, 600);
});

loadGlobalMemo();
loadWorkspaces();
