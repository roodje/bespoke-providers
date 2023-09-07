package com.yolt.providers.stet.generic.service.pec.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class StetRawBankPaymentStatusMapperTest {

    private StetRawBankPaymentStatusMapper rawBankPaymentStatusMapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        rawBankPaymentStatusMapper = new StetRawBankPaymentStatusMapper(objectMapper);
    }

    static Stream<String> missingStatusOrMalformedRawResponses() {
        return Stream.of(null, "", "{}", "{\"message\":\"MESSAGE\"}");
    }

    @MethodSource("missingStatusOrMalformedRawResponses")
    @ParameterizedTest
    void shouldMapToUnknownStatusAndReasonAsRawResponse(String givenRawResponse) {
        // when
        RawBankPaymentStatus rawBankPaymentStatus = rawBankPaymentStatusMapper.mapBankPaymentStatus(givenRawResponse);

        // then
        assertThat(rawBankPaymentStatus).satisfies(paymentStatus -> {
            assertThat(paymentStatus.getStatus()).isEqualTo("UNKNOWN");
            assertThat(paymentStatus.getReason()).isEqualTo(Objects.isNull(givenRawResponse) ? "" : givenRawResponse);
        });
    }

    @Test
    void shouldToStatusAndReason() {
        // given
        String rawResponse = "{\"error\":\"ERROR\",\"message\":\"MESSAGE\"}";

        // when
        RawBankPaymentStatus rawBankPaymentStatus = rawBankPaymentStatusMapper.mapBankPaymentStatus(rawResponse);

        // then
        assertThat(rawBankPaymentStatus).satisfies(paymentStatus -> {
            assertThat(paymentStatus.getStatus()).isEqualTo("ERROR");
            assertThat(paymentStatus.getReason()).isEqualTo("MESSAGE");
        });
    }

    @Test
    void shouldMapToStatusAndReasonAsRawResponse() {
        // given
        String rawResponse = "{\"error\":\"ERROR\"}";

        // when
        RawBankPaymentStatus rawBankPaymentStatus = rawBankPaymentStatusMapper.mapBankPaymentStatus(rawResponse);

        // then
        assertThat(rawBankPaymentStatus).satisfies(paymentStatus -> {
            assertThat(paymentStatus.getStatus()).isEqualTo("ERROR");
            assertThat(paymentStatus.getReason()).isEqualTo(rawResponse);
        });
    }
}
