package com.yolt.providers.monorepogroup.qontogroup.common.mapper;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class QontoGroupDateMapperTest {

    private Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private ZoneId zoneId = ZoneId.of("Europe/Paris");

    private QontoGroupDateMapper dateMapper = new QontoGroupDateMapper(zoneId, clock);

    @Test
    void shouldMapToHttpRequestDateFormat() {
        //given
        Instant requestTime = Instant.now(clock);
        String expectedFormattedDate = "2022-01-01T00:00:00.000Z";

        //when
        var result = dateMapper.mapHttpRequestDateFormat(requestTime);

        //then
        assertThat(result).isEqualTo(expectedFormattedDate);
    }

    @Test
    void shouldReturnZonedDateTimeWithTheSameZoneAsClock() {
        //given
        var expectedResult = ZonedDateTime.now(clock);

        //when
        var result = dateMapper.getZonedDateTimeWithDefaultClockZoneId();

        //then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldReturnZonedDateTimeInGivenZone() {
        //given
        var expectedResult = ZonedDateTime.now(clock).withZoneSameInstant(zoneId);

        //when
        var result = dateMapper.getZoneDateTime();

        //then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldMappedOffsetDateTimeToZonedDateTime() {
        //given
        var dateToMap = OffsetDateTime.now(clock);
        var expectedResult = ZonedDateTime.of(2022, 01, 01, 01, 00, 00, 0, zoneId);

        //when
        var result = dateMapper.toZonedDateTime(dateToMap);

        //then
        assertThat(result).isEqualTo(expectedResult);
    }
}