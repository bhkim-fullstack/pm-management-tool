package com.platformerz.pmtool.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;

/**
 * The SQLite dialect's native timestamp binding/parsing is unreliable
 * (mismatched write/read formats), so Instant is stored as a plain ISO-8601
 * TEXT column instead of relying on Hibernate's temporal JDBC type.
 */
@Converter
public class InstantStringConverter implements AttributeConverter<Instant, String> {

	@Override
	public String convertToDatabaseColumn(Instant attribute) {
		return attribute == null ? null : attribute.toString();
	}

	@Override
	public Instant convertToEntityAttribute(String dbData) {
		return dbData == null ? null : Instant.parse(dbData);
	}

}
