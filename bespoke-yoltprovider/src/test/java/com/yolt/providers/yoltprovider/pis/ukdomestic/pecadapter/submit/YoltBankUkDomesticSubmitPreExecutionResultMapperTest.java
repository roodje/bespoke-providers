package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.yoltprovider.pis.TestPaymentAuthMeansUtil;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YoltBankUkDomesticSubmitPreExecutionResultMapperTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private YoltBankUkDomesticSubmitPreExecutionResultMapper subject;


    @ParameterizedTest
    @ValueSource(strings = {"http://localhost?payment_id=", "http://localhost#payment_id="})
    void shouldReturnUkSubmitPreExecutionResultForMapWhenCorrectData(String redirectUrl) throws JsonProcessingException {
        // given
        when(objectMapper.readValue(eq("fakeProviderState"), eq(UkProviderState.class))).thenReturn(new UkProviderState(null, PaymentType.SINGLE, null));
        UUID clientId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest(
                "fakeProviderState",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(clientId, UUID.randomUUID(), UUID.randomUUID()),
                redirectUrl + paymentId,
                null,
                null,
                null,
                null
        );

        // when
        YoltBankUkSubmitPreExecutionResult result = subject.map(submitPaymentRequest);

        // then
        assertThat(result).extracting(YoltBankUkSubmitPreExecutionResult::getClientId, YoltBankUkSubmitPreExecutionResult::getPaymentId, YoltBankUkSubmitPreExecutionResult::getPaymentType)
                .contains(clientId, paymentId, PaymentType.SINGLE);
    }

    @Test
    void shouldThrowExceptionWhenObjectMappingFails() throws JsonProcessingException {
        // given
        UUID clientId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest(
                "fakeProviderState",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(clientId, UUID.randomUUID(), UUID.randomUUID()),
                "http://localhost?payment_id=" + paymentId,
                null,
                null,
                null,
                null
        );

        doThrow(new JsonProcessingException("error") {
        }).when(objectMapper).readValue(anyString(), eq(UkProviderState.class));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.map(submitPaymentRequest);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find payment type in providerState during payment submission");
    }
}