package com.platformerz.pmtool.web;

import java.time.LocalDate;

// start/end are inclusive, matching Task.startDate/endDate directly.
public record TaskRequest(String title, Long personId, LocalDate start, LocalDate end) {
}
