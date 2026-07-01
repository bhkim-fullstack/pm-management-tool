package com.platformerz.pmtool.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class Memo {

	@Id
	private Long projectId;

	@Lob
	@Column(length = 100_000)
	private String content = "";

	protected Memo() {
	}

	public Memo(Long projectId) {
		this.projectId = projectId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
