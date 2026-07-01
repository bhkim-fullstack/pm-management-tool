package com.platformerz.pmtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class PmManagementToolApplication {

	public static void main(String[] args) throws IOException {
		// SQLite JDBC driver requires the parent directory to already exist.
		Files.createDirectories(Path.of("data"));
		SpringApplication.run(PmManagementToolApplication.class, args);
	}

}
