package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
class AbnAmroRawBankPaymentStatusMapperTest {

    @InjectMocks
    private AbnAmroRawBankPaymentStatusMapper subject;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonNode responseBodyNode;

    @Mock
    private ArrayNode errorsNode;

    @Mock
    private JsonNode errorNode;

    @Mock
    private JsonNode textNode;

    @Test
    void shouldReturnRawBankPaymentStatusWithParsedErrorRawResponseBodyForMapBankPaymentStatusWhenRawBodyResponseComesFromFailureApiCall() throws JsonProcessingException {
        // given
        String rawBodyResponse = "errorRawBodyResponse";

        given(objectMapper.readTree(anyString()))
                .willReturn(responseBodyNode);
        given(responseBodyNode.get(anyString()))
                .willReturn(errorsNode);
        given(errorsNode.isArray())
                .willReturn(true);
        given(errorsNode.get(anyInt()))
                .willReturn(errorNode);
        given(errorNode.get(anyString()))
                .willReturn(textNode, textNode);
        given(textNode.asText())
                .willReturn("errorCode", "errorMessage");

        // when
        RawBankPaymentStatus result = subject.mapBankPaymentStatus(rawBodyResponse);

        // then
        then(objectMapper)
                .should()
                .readTree("errorRawBodyResponse");
        then(responseBodyNode)
                .should()
                .get("errors");
        then(errorsNode)
                .should()
                .isArray();
        then(errorsNode)
                .should()
                .get(0);
        then(errorNode)
                .should()
                .get("code");
        then(errorNode)
                .should()
                .get("message");
        then(textNode)
                .should(times(2))
                .asText();

        assertThat(result).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("errorCode", "errorMessage");
    }

    @Test
    void shouldReturnRawBankPaymentStatusWithoutParsingRawBodyResponseForMapBankPaymentStatusWhenRawBodyResponseDoesNotComeFromFailureApiCall() throws JsonProcessingException {
        // given
        String rawBodyResponse = "rawBodyResponse";

        given(objectMapper.readTree(anyString()))
                .willReturn(responseBodyNode);
        given(responseBodyNode.get(anyString()))
                .willReturn(errorsNode);
        given(errorsNode.isArray())
                .willReturn(false);

        // when
        RawBankPaymentStatus result = subject.mapBankPaymentStatus(rawBodyResponse);

        // then
        assertThat(result).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("rawBodyResponse", "");
    }
}