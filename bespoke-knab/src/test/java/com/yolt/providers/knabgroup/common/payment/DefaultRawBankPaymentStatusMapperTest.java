package com.yolt.providers.knabgroup.common.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.knabgroup.knab.config.KnabBeanConfigV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRawBankPaymentStatusMapperTest {

    private DefaultRawBankPaymentStatusMapper subject;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new KnabBeanConfigV2().getObjectMapper();
        subject = new DefaultRawBankPaymentStatusMapper(objectMapper);
    }

    @Test
    void shouldReturnRawBankPaymentStatusWithParsedErrorRawResponseBodyForMapBankPaymentStatusWhenRawBodyResponseComesFromFailureApiCall() {
        // given
        String rawBodyResponse = "{\"tppMessages\":[{\"code\":\"errorCode\",\"text\":\"errorText\"}]}";
        RawBankPaymentStatus expectedResult = RawBankPaymentStatus.forStatus("errorCode", "errorText");

        // when
        RawBankPaymentStatus result = subject.mapBankPaymentStatus(rawBodyResponse);

        // then
        assertThat(result).usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldReturnRawBankPaymentStatusWithoutParsingRawBodyResponseForMapBankPaymentStatusWhenRawBodyResponseDoesNotComeFromFailureApiCall() {
        // given
        String rawBodyResponse = "{\"raw\":\"soRaw\"}";
        RawBankPaymentStatus expectedResult = RawBankPaymentStatus.forStatus(rawBodyResponse, "");

        // when
        RawBankPaymentStatus result = subject.mapBankPaymentStatus(rawBodyResponse);

        // then
        assertThat(result).usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }
}