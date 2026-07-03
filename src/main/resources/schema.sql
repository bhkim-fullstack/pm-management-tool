CREATE TABLE IF NOT EXISTS workspace (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name VARCHAR(255) NOT NULL,
	created_at TEXT NOT NULL
);

INSERT INTO workspace (id, name, created_at)
SELECT 1, '기본', strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
WHERE NOT EXISTS (SELECT 1 FROM workspace);

CREATE TABLE IF NOT EXISTS project (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	workspace_id INTEGER NOT NULL DEFAULT 1 REFERENCES workspace (id),
	name VARCHAR(255) NOT NULL,
	created_at TEXT NOT NULL,
	color_index INTEGER NOT NULL DEFAULT 0,
	color TEXT NOT NULL DEFAULT '#0969da'
);

CREATE TABLE IF NOT EXISTS person (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	project_id INTEGER NOT NULL REFERENCES project (id),
	name VARCHAR(255) NOT NULL,
	color_index INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS task (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	project_id INTEGER NOT NULL REFERENCES project (id),
	person_id INTEGER REFERENCES person (id),
	title VARCHAR(255) NOT NULL,
	start_date TEXT NOT NULL,
	end_date TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS task_person (
	task_id INTEGER NOT NULL REFERENCES task (id),
	person_id INTEGER NOT NULL REFERENCES person (id),
	PRIMARY KEY (task_id, person_id)
);

-- Backfill the old single-assignee column into the new many-to-many table,
-- then clear it so a later removal in task_person can't be resurrected by
-- this backfill running again on the next startup.
INSERT INTO task_person (task_id, person_id)
SELECT task.id, task.person_id FROM task
WHERE task.person_id IS NOT NULL
	AND NOT EXISTS (SELECT 1 FROM task_person WHERE task_person.task_id = task.id);

UPDATE task SET person_id = NULL WHERE person_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS memo (
	project_id INTEGER PRIMARY KEY REFERENCES project (id),
	content TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS global_memo (
	id INTEGER PRIMARY KEY,
	content TEXT NOT NULL
);
