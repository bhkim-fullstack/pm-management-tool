package com.platformerz.pmtool.web;

import java.time.LocalDate;
import java.util.List;

// start/end are inclusive, matching Task.startDate/endDate directly.
public record TaskRequest(String title, List<Long> personIds, LocalDate start, LocalDate end) {
}
