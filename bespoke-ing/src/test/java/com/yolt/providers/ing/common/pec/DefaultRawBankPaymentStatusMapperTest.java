package com.yolt.providers.ing.common.pec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.ing.common.config.PisBeanConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRawBankPaymentStatusMapperTest {

    private DefaultRawBankPaymentStatusMapper sut;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new PisBeanConfig().ingPisObjectMapper();
        sut = new DefaultRawBankPaymentStatusMapper(objectMapper);
    }

    @Test
    void shouldReturnRawBankPaymentStatusWithParsedErrorRawResponseBodyForMapBankPaymentStatusWhenRawBodyResponseComesFromFailureApiCall() {
        // given
        var rawBodyResponse = "{\"tppMessages\":[{\"code\":\"errorCode\",\"text\":\"errorText\"}]}";

        // when
        var result = sut.mapBankPaymentStatus(rawBodyResponse);

        // then
        assertThat(result).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("errorCode", "errorText");
    }

    @Test
    void shouldReturnRawBankPaymentStatusWithoutParsingRawBodyResponseForMapBankPaymentStatusWhenRawBodyResponseDoesNotComeFromFailureApiCall() {
        // given
        String rawBodyResponse = "{\"raw\":\"soRaw\"}";

        // when
        RawBankPaymentStatus result = sut.mapBankPaymentStatus(rawBodyResponse);

        // then
        assertThat(result).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains(rawBodyResponse, "");
    }

}