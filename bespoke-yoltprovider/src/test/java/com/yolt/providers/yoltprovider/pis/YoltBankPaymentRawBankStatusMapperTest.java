package com.yolt.providers.yoltprovider.pis;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class YoltBankPaymentRawBankStatusMapperTest {

    @InjectMocks
    private YoltBankPaymentRawBankStatusMapper rawBankStatusMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnRawBankPaymentStatus() throws JsonProcessingException {
        // given
        String fakeRawBody = "fakeRawBody";
        JsonNode expectedJsonNode = mock(JsonNode.class);
        JsonNode expectedTextNode = mock(JsonNode.class);
        given(objectMapper.readTree(anyString()))
                .willReturn(expectedJsonNode);
        given(expectedJsonNode.get(anyString()))
                .willReturn(expectedTextNode);
        given(expectedTextNode.asText())
                .willReturn("fakeCode", "fakeMessage");

        // when
        RawBankPaymentStatus result = rawBankStatusMapper.mapBankPaymentStatus(fakeRawBody);

        // then
        assertThat(result).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("fakeCode", "fakeMessage");
        then(objectMapper)
                .should()
                .readTree("fakeRawBody");
        then(expectedJsonNode)
                .should()
                .get("code");
        then(expectedJsonNode)
                .should()
                .get("message");
        then(expectedTextNode)
                .should(times(2))
                .asText();
    }
}
