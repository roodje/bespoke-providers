package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.TestPaymentAuthMeansUtil;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.SepaProviderState;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit.YoltBankSepaSubmitPreExecutionResult;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaStatusPaymentPreExecutionResultMapperTest {

    @InjectMocks
    private YoltBankSepaStatusPaymentPreExecutionResultMapper preExecutionMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnSepaSubmitPreExecutionResultForMapWhenCorrectData() throws JsonProcessingException {
        // given
        Signer signer = mock(Signer.class);
        UUID clientId = UUID.randomUUID();
        UUID privateKid = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        GetStatusRequest getStatusRequest = new GetStatusRequest(
                "fakeProviderState",
                "fakePaymentId",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(clientId, publicKid, privateKid),
                signer,
                null,
                null,
                null
        );
        given(objectMapper.readValue("fakeProviderState", SepaProviderState.class))
                .willReturn(new SepaProviderState(
                        "fakePaymentId",
                        PaymentType.SINGLE
                ));
        // when
        YoltBankSepaSubmitPreExecutionResult result = preExecutionMapper.map(getStatusRequest);

        // then
        assertThat(result).extracting(
                YoltBankSepaSubmitPreExecutionResult::getPaymentId,
                YoltBankSepaSubmitPreExecutionResult::getSigner)
                .contains("fakePaymentId", signer);
        assertThat(result.getAuthenticationMeans()).extracting(PaymentAuthenticationMeans::getClientId,
                PaymentAuthenticationMeans::getPublicKid,
                PaymentAuthenticationMeans::getSigningKid)
                .contains(clientId, publicKid, privateKid);
    }

    @Test
    void shouldThrowErrorWhenMappingFails() throws JsonProcessingException {
        //given
        Signer signer = mock(Signer.class);
        UUID clientId = UUID.randomUUID();
        UUID privateKid = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        GetStatusRequest getStatusRequest = new GetStatusRequest(
                "fakeProviderState",
                "fakePaymentId",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(clientId, publicKid, privateKid),
                signer,
                null,
                null,
                null
        );
        doThrow(new RuntimeException()).when(objectMapper).readValue(anyString(), any(Class.class));
        //when
        ThrowableAssert.ThrowingCallable callable = () -> preExecutionMapper.map(getStatusRequest);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Error when retrieving payment type from provider state during status retrieval");
    }
}
