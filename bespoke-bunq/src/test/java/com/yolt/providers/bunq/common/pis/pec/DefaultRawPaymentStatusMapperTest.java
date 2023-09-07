package com.yolt.providers.bunq.common.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRawPaymentStatusMapperTest {

    @Mock
    ObjectMapper objectMapper;

    private DefaultRawPaymentStatusMapper rawPaymentStatusMapper;

    @BeforeEach
    void setUp() {
        rawPaymentStatusMapper = new DefaultRawPaymentStatusMapper(objectMapper);
    }

    @Test
    void shouldReturnRawPaymentStatusesWithErrorCodeAndDescriptionProvidedInResponse() throws JsonProcessingException {
        //given
        var mainNode = mock(JsonNode.class);
        when(objectMapper.readTree("rawResponse")).thenReturn(mainNode);
        var errorArray = mock(ArrayNode.class);
        when(errorArray.isArray()).thenReturn(true);
        when(mainNode.get("Error")).thenReturn(errorArray);
        var elementOfErrorArray = mock(JsonNode.class);
        when(errorArray.get(0)).thenReturn(elementOfErrorArray);
        var textNodeWithErrorDescription = mock(TextNode.class);
        when(textNodeWithErrorDescription.asText()).thenReturn("Error Description");
        var textNodeWithErrorDescriptionTranslated = mock(TextNode.class);
        when(textNodeWithErrorDescriptionTranslated.asText()).thenReturn("Error Description Translated");
        when(elementOfErrorArray.get("error_description")).thenReturn(textNodeWithErrorDescription);
        when(elementOfErrorArray.get("error_description_translated")).thenReturn(textNodeWithErrorDescriptionTranslated);
        var expectedRawPaymentStatuses = RawBankPaymentStatus.unknown("Error Description. Error Description Translated");

        //when
        var result = rawPaymentStatusMapper.mapBankPaymentStatus("rawResponse");

        //then
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedRawPaymentStatuses);
    }

    @Test
    void shouldReturnRawPaymentStatusesWithRawBankResponseAsReason() throws JsonProcessingException {
        var mainNode = mock(JsonNode.class);
        when(objectMapper.readTree("rawResponse")).thenReturn(mainNode);
        var errorArray = mock(ArrayNode.class);
        when(errorArray.isArray()).thenReturn(false);
        when(mainNode.get("Error")).thenReturn(errorArray);
        var expectedRawPaymentStatuses = RawBankPaymentStatus.unknown("rawResponse");

        //when
        var result = rawPaymentStatusMapper.mapBankPaymentStatus("rawResponse");

        //then
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedRawPaymentStatuses);
    }

}