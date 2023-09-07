package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DefaultRaiffeisenAtGroupDateMapperTest {

    private ZoneId zoneId = ZoneId.systemDefault();
    private Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));

    private DefaultRaiffeisenAtGroupDateMapper dateMapper = new DefaultRaiffeisenAtGroupDateMapper(zoneId, clock);

    @Test
    void shouldReturnProperlyMappedDate() {
        //given
        var localDate = LocalDate.now(zoneId);
        var expectedDate = Date.from(localDate.atStartOfDay(zoneId).toInstant());

        //when
        var result = dateMapper.toDate(localDate);

        //then
        assertThat(result).isEqualTo(expectedDate);
    }

    @Test
    void shouldReturnDateAsString() {
        //given
        var instantDate = Instant.now();
        var expectedStringDate = DateTimeFormatter.ISO_LOCAL_DATE.withZone(zoneId).format(instantDate);

        //when
        var result = dateMapper.toDateFormat(instantDate);

        //then
        assertThat(result).isEqualTo(expectedStringDate);
    }

    @Test
    void shouldReturnZonedDateTime() {
        //given
        var expectedResult = ZonedDateTime.now(clock);
        //when
        var result = dateMapper.getZonedDateTime();

        //then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldMappedLocalDateToZoneDateTime() {
        //given
        var localDate = LocalDate.of(2022, 07, 11);
        var expectedResult = localDate.atStartOfDay(zoneId);
        //when
        var result = dateMapper.getZonedDateTime(localDate);

        //then
        assertThat(result).isEqualTo(expectedResult);
    }
}