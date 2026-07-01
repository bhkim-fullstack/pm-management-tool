package com.platformerz.pmtool.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;

/**
 * See InstantStringConverter: SQLite's native date handling is unreliable,
 * so LocalDate is stored as a plain ISO-8601 (yyyy-MM-dd) TEXT column.
 */
@Converter
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {

	@Override
	public String convertToDatabaseColumn(LocalDate attribute) {
		return attribute == null ? null : attribute.toString();
	}

	@Override
	public LocalDate convertToEntityAttribute(String dbData) {
		return dbData == null ? null : LocalDate.parse(dbData);
	}

}
