package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.service.frombookingdatetimeformatter;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PermanentTsbGroupFromBookingDateTimeFormatterTest {

    private final PermanentTsbGroupFromBookingDateTimeFormatter fromBookingDateTimeFormatter = new PermanentTsbGroupFromBookingDateTimeFormatter();

    @Test
    void shouldConvertFromBookingDateTimeToRequiredFormat() {
        // given
        Instant fromBookingDateTime = Clock.systemUTC().instant();

        // when
        String formattedFromBookingDateTime = fromBookingDateTimeFormatter.apply(fromBookingDateTime);

        // then
        assertThat(formattedFromBookingDateTime).matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$");
    }
}
