package com.yolt.providers.rabobank.pis.pec;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabobankSepaPaymentRawBankStatusMapperTest {

    @InjectMocks
    private RabobankSepaPaymentRawBankStatusMapper subject;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnRawBankPaymentStatusWithWholeRawResponseBody() throws JsonProcessingException {
        //given
        String rawResponseBody = "someRawResponseBody";
        JsonNode messagesNode = mock(JsonNode.class);
        when(messagesNode.isArray()).thenReturn(false);
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.get("tppMessages")).thenReturn(messagesNode);
        when(objectMapper.readTree(rawResponseBody)).thenReturn(jsonNode);

        //when
        RawBankPaymentStatus returnedRawStatus = subject.mapBankPaymentStatus(rawResponseBody);

        //then
        assertThat(returnedRawStatus).extracting(RawBankPaymentStatus::getStatus,
                RawBankPaymentStatus::getReason)
                .contains("someRawResponseBody", "");
    }

    @Test
    void shouldReturnRawBankPaymentStatusWithDetailedStatusDescription() throws JsonProcessingException {
        String rawResponseBody = "someRawResponseBody";
        JsonNode codeNode = mock(JsonNode.class);
        when(codeNode.asText()).thenReturn("ERROR_CODE");
        JsonNode textNode = mock(JsonNode.class);
        when(textNode.asText()).thenReturn("Error text");
        JsonNode detailsNode = mock(JsonNode.class);
        when(detailsNode.get("code")).thenReturn(codeNode);
        when(detailsNode.get("text")).thenReturn(textNode);
        ArrayNode messagesNode = mock(ArrayNode.class);
        when(messagesNode.isArray()).thenReturn(true);
        when(messagesNode.get(0)).thenReturn(detailsNode);
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.get("tppMessages")).thenReturn(messagesNode);
        when(objectMapper.readTree(rawResponseBody)).thenReturn(jsonNode);

        //when
        RawBankPaymentStatus returnedRawStatus = subject.mapBankPaymentStatus(rawResponseBody);

        //then
        assertThat(returnedRawStatus).extracting(RawBankPaymentStatus::getStatus,
                RawBankPaymentStatus::getReason)
                .contains("ERROR_CODE", "Error text");
    }
}
