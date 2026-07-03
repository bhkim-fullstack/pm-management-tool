package com.platformerz.pmtool.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * schema.sql only runs CREATE TABLE IF NOT EXISTS, so it can't add columns to
 * a database that already exists from a previous version of the app. This
 * runner adds any such columns on startup, guarded by a check so it's a
 * no-op once the column is present.
 */
@Component
public class SchemaMigrationRunner implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	public SchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!hasColumn("project", "color_index")) {
			jdbcTemplate.execute("ALTER TABLE project ADD COLUMN color_index INTEGER NOT NULL DEFAULT 0");
		}
		if (!hasColumn("project", "color")) {
			jdbcTemplate.execute("ALTER TABLE project ADD COLUMN color TEXT NOT NULL DEFAULT '#0969da'");
			jdbcTemplate.execute("""
				UPDATE project SET color = CASE color_index % 8
					WHEN 0 THEN '#0969da'
					WHEN 1 THEN '#8250df'
					WHEN 2 THEN '#1a7f37'
					WHEN 3 THEN '#bf3989'
					WHEN 4 THEN '#d1242f'
					WHEN 5 THEN '#9a6700'
					WHEN 6 THEN '#0550ae'
					ELSE '#57606a'
				END
				""");
		}
		if (!hasColumn("project", "workspace_id")) {
			jdbcTemplate.execute("ALTER TABLE project ADD COLUMN workspace_id INTEGER NOT NULL DEFAULT 1");
		}
	}

	private boolean hasColumn(String table, String column) {
		return jdbcTemplate.queryForList("PRAGMA table_info(" + table + ")").stream()
			.anyMatch(row -> column.equalsIgnoreCase((String) row.get("name")));
	}

}
