package com.yolt.providers.monorepogroup.chebancagroup.common.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

public interface CheBancaGroupDateMapper {

    Date toDate(final LocalDate localDate);

    String toDateFormat(Instant instant);

    ZonedDateTime getZonedDateTime();

    ZonedDateTime getZonedDateTime(LocalDate date);
}
