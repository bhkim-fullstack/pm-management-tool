package com.platformerz.pmtool.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class GlobalMemo {

	public static final Long SINGLETON_ID = 1L;

	@Id
	private Long id = SINGLETON_ID;

	@Lob
	@Column(length = 100_000)
	private String content = "";

	public Long getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
