package com.yolt.providers.stet.generic.mapper;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateTimeSupplierParsingTest {

    private DateTimeSupplier dateTimeSupplier;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy-MM-dd][yyyy/MM/dd][dd-MM-yyyy][dd/MM/yyyy]");

    @BeforeEach
    public void setup() {
        Clock clock = Clock.systemDefaultZone();
        ZoneId zoneId = ZoneId.of("Europe/Paris");
        dateTimeSupplier = new DateTimeSupplier(clock, zoneId);
    }

    @Test
    public void shouldReturnCorrectDate() {
        //given
        String dataInString = "2022-01-19";
        OffsetDateTime expectedDate = OffsetDateTime.of(2022, 01, 19, 0, 0, 0, 0, ZoneOffset.of("+1"));

        //when
        OffsetDateTime dateTime = dateTimeSupplier.parseToOffsetDateTime(dataInString, formatter);
        //then
        assertThat(dateTime).isEqualTo(expectedDate);

    }

    @Test
    public void shouldReturnNullForEmptyString() {
        //given
        String emptyString ="";
        //when
        OffsetDateTime dateTime = dateTimeSupplier.parseToOffsetDateTime(emptyString, formatter);
        //then
        assertThat(dateTime).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenInputStringIsNotADate() {
        //given
        String input = "This is definitely not a date";
        //when
        ThrowableAssert.ThrowingCallable parseDate = () -> dateTimeSupplier.parseToOffsetDateTime(input, formatter);
        //then
        assertThatThrownBy(parseDate).isInstanceOf(DateTimeParseException.class)
                .withFailMessage("Text " + input + " could not be parsed");
    }

    @Test
    public void shouldReturnNullWhenInputStringIsNull() {

        //when
        OffsetDateTime dateTime = dateTimeSupplier.parseToOffsetDateTime(null, formatter);
        //then
        assertThat(dateTime).isNull();

    }

}