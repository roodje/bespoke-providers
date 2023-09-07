package com.yolt.providers.openbanking.ais.generic2.pec.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedRawBodyResponseException;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBError1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBErrorResponse1;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericRawBankPaymentStatusMapperTest {

    @InjectMocks
    private GenericRawBankPaymentStatusMapper subject;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonNode jsonNode;

    @Test
    void shouldReturnRawBankPaymentStatusWhenErrorRawResponseBodyIsProvided() throws JsonProcessingException {
        // given
        String rawResponseBody = "errorRawResponseBody";

        List<OBError1> errors = Collections.emptyList();
        OBErrorResponse1 errorResponse = new OBErrorResponse1()
                .code("errorCode")
                .errors(errors);

        given(objectMapper.readValue(anyString(), eq(OBErrorResponse1.class)))
                .willReturn(errorResponse);
        given(objectMapper.writeValueAsString(any(List.class)))
                .willReturn("specificReason");

        // when
        RawBankPaymentStatus result = subject.mapBankPaymentStatus(rawResponseBody);

        // then
        then(objectMapper)
                .should()
                .readValue("errorRawResponseBody", OBErrorResponse1.class);
        then(objectMapper)
                .should()
                .writeValueAsString(errors);

        assertThat(result).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("errorCode", "specificReason");
    }

    @Test
    void shouldThrowMalformedRawBodyResponseExceptionWhenErrorRawResponseBodyIsProvidedAndCannotSerializeListOfErrors() throws JsonProcessingException {
        // given
        String rawResponseBody = "errorRawResponseBody";
        ;
        List<OBError1> errors = Collections.emptyList();
        OBErrorResponse1 errorResponse = new OBErrorResponse1()
                .code("errorCode")
                .errors(errors);

        given(objectMapper.readValue(anyString(), eq(OBErrorResponse1.class)))
                .willReturn(errorResponse);
        given(objectMapper.writeValueAsString(any(List.class)))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.mapBankPaymentStatus(rawResponseBody);

        // then
        assertThatExceptionOfType(MalformedRawBodyResponseException.class)
                .isThrownBy(callable)
                .withMessage("Unable to serialize errors array from bank");
    }

    @Test
    void shouldReturnRawBankPaymentStatusWithEntireRawResponseBodyAsReasonWhen2xxRawResponseBodyWithoutStatusIsProvided() throws JsonProcessingException {
        // given
        String rawResponseBody = "2xxRawResponseBodyWithoutStatus";

        given(objectMapper.readValue(anyString(), eq(OBErrorResponse1.class)))
                .willReturn(new OBErrorResponse1());
        given(objectMapper.readTree(anyString()))
                .willReturn(jsonNode);
        given(jsonNode.findPath(anyString()))
                .willReturn(JsonNodeFactory.instance.missingNode());

        // when
        RawBankPaymentStatus result = subject.mapBankPaymentStatus(rawResponseBody);

        // then
        then(objectMapper)
                .should()
                .readValue("2xxRawResponseBodyWithoutStatus", OBErrorResponse1.class);
        then(objectMapper)
                .should()
                .readTree("2xxRawResponseBodyWithoutStatus");
        then(jsonNode)
                .should()
                .findPath("Status");

        assertThat(result).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("UNKNOWN", "2xxRawResponseBodyWithoutStatus");
    }

    @Test
    void shouldReturnRawBankPaymentStatusWithStatusAndEntireRawResponseBodyAsReasonWhen2xxRawResponseBodyWithStatusIsProvided() throws JsonProcessingException {
        // given
        String rawResponseBody = "2xxRawResponseBodyWithStatus";

        given(objectMapper.readValue(anyString(), eq(OBErrorResponse1.class)))
                .willReturn(new OBErrorResponse1());
        given(objectMapper.readTree(anyString()))
                .willReturn(jsonNode);
        given(jsonNode.findPath(anyString()))
                .willReturn(jsonNode);
        given(jsonNode.asText())
                .willReturn("status");

        // when
        RawBankPaymentStatus result = subject.mapBankPaymentStatus(rawResponseBody);

        // then
        then(objectMapper)
                .should()
                .readValue("2xxRawResponseBodyWithStatus", OBErrorResponse1.class);
        then(objectMapper)
                .should()
                .readTree("2xxRawResponseBodyWithStatus");
        then(jsonNode)
                .should()
                .findPath("Status");
        then(jsonNode)
                .should()
                .asText();

        assertThat(result).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("status", "2xxRawResponseBodyWithStatus");
    }

    @Test
    void shouldThrowMalformedRawBodyResponseExceptionWhenProvided2xxRawBodyResponseIsNotInJsonForm() throws JsonProcessingException {
        // given
        String rawResponseBody = "malformedJson";

        given(objectMapper.readValue(anyString(), eq(OBErrorResponse1.class)))
                .willReturn(new OBErrorResponse1());
        given(objectMapper.readTree(anyString()))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.mapBankPaymentStatus(rawResponseBody);

        // then
        assertThatExceptionOfType(MalformedRawBodyResponseException.class)
                .isThrownBy(callable)
                .withMessage("Unable to parse rawBodyResponse from bank")
                .withCauseInstanceOf(JsonProcessingException.class);
    }
}