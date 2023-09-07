package com.yolt.providers.cbiglobe.common.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CbiGlobeRawBankPaymentStatusMapperTest {

    @InjectMocks
    private CbiGlobeRawBankPaymentStatusMapper subject;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonNode jsonNode;

    @Test
    void shouldReturnRawBankPaymentStatusForMapBankPaymentStatusWhenCorrectData() throws JsonProcessingException {
        // given
        var rawBodyResponse = "rawBodyResponse";

        given(objectMapper.readTree(anyString()))
                .willReturn(jsonNode);
        given(jsonNode.findPath(anyString()))
                .willReturn(jsonNode);
        given(jsonNode.isMissingNode())
                .willReturn(false);
        given(jsonNode.get(anyInt()))
                .willReturn(jsonNode);
        given(jsonNode.get(anyString()))
                .willReturn(jsonNode);
        given(jsonNode.asText())
                .willReturn("errorCode", "errorMsg");

        // when
        var result = subject.mapBankPaymentStatus(rawBodyResponse);

        // then
        then(objectMapper)
                .should()
                .readTree("rawBodyResponse");
        then(jsonNode)
                .should()
                .findPath("tppMessages");
        then(jsonNode)
                .should()
                .isMissingNode();
        then(jsonNode)
                .should()
                .get(0);
        then(jsonNode)
                .should()
                .get("code");
        then(jsonNode)
                .should()
                .get("text");
        then(jsonNode)
                .should(times(2))
                .asText();
        assertThat(result).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("errorCode", "errorMsg");
    }

    @Test
    void shouldReturnRawBankPaymentStatusWithUnknownStatusAndRawResponseBodyAsReasonForMapBankPaymentStatusWhenRawBodyResponseHasNonErrorForm() throws JsonProcessingException {
        // given
        var rawBodyResponse = "rawBodyResponse";

        given(objectMapper.readTree(anyString()))
                .willReturn(jsonNode);
        given(jsonNode.findPath(anyString()))
                .willReturn(jsonNode);
        given(jsonNode.isMissingNode())
                .willReturn(true);

        // when
        var result = subject.mapBankPaymentStatus(rawBodyResponse);

        // then
        assertThat(result).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("UNKNOWN", "rawBodyResponse");
    }
}