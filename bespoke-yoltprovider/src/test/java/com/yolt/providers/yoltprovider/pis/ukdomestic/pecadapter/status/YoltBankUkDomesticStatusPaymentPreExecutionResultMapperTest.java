package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.yoltprovider.pis.TestPaymentAuthMeansUtil;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.YoltBankUkSubmitPreExecutionResult;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YoltBankUkDomesticStatusPaymentPreExecutionResultMapperTest {

    @InjectMocks
    private YoltBankUkDomesticStatusPaymentPreExecutionResultMapper preExecutionResultMapper;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws JsonProcessingException {
    }

    @Test
    void shouldReturnUkSubmitPreExecutionResultForMapWhenCorrectData() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(eq("fakeProviderState"), eq(UkProviderState.class))).thenReturn(new UkProviderState(null, PaymentType.SINGLE, null));
        UUID clientId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        GetStatusRequest getStatusRequest = new GetStatusRequest("fakeProviderState",
                paymentId.toString(),
                TestPaymentAuthMeansUtil.getBasicAuthMeans(clientId, UUID.randomUUID(), UUID.randomUUID()),
                null,
                null,
                null,
                null
        );

        // when
        YoltBankUkSubmitPreExecutionResult result = preExecutionResultMapper.map(getStatusRequest);

        // then
        assertThat(result).extracting(YoltBankUkSubmitPreExecutionResult::getPaymentId, YoltBankUkSubmitPreExecutionResult::getClientId)
                .contains(paymentId, clientId);
    }

    @Test
    void shouldThrowExceptionWhenObjectMappingFails() throws JsonProcessingException {
        // given
        UUID clientId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        GetStatusRequest getStatusRequest = new GetStatusRequest("fakeProviderState",
                paymentId.toString(),
                TestPaymentAuthMeansUtil.getBasicAuthMeans(clientId, UUID.randomUUID(), UUID.randomUUID()),
                null,
                null,
                null,
                null
        );
        doThrow(new JsonProcessingException("error") {
        }).when(objectMapper).readValue(anyString(), any(Class.class));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> preExecutionResultMapper.map(getStatusRequest);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Error when retrieving payment type from provider state during status retrieval");
    }
}