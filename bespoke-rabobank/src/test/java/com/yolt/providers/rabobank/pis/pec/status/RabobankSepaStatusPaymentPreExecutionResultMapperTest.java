package com.yolt.providers.rabobank.pis.pec.status;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.GetStatusRequestBuilder;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.RabobankSampleTypedAuthenticationMeans;
import com.yolt.providers.rabobank.pis.pec.RabobankPaymentProviderState;
import com.yolt.providers.rabobank.pis.pec.RabobankPaymentProviderStateDeserializer;
import com.yolt.providers.rabobank.pis.pec.submit.RabobankSepaSubmitPaymentPreExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RabobankSepaStatusPaymentPreExecutionResultMapperTest {

    @InjectMocks
    private RabobankSepaStatusPaymentPreExecutionResultMapper subject;

    private final RabobankSampleTypedAuthenticationMeans sampleTypedAuthenticationMeans = new RabobankSampleTypedAuthenticationMeans();

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private RabobankPaymentProviderStateDeserializer providerStateDeserializer;

    @Test
    void shouldReturnPreExecutionResultWithPaymentIdTakenFromRequestWhenPaymentIdIsProvidedInRequest() {
        //given
        GetStatusRequest getStatusRequest = createGetStatusRequest(true);

        //when
        RabobankSepaSubmitPaymentPreExecutionResult preExecutionResult = subject.map(getStatusRequest);

        //then
        assertThat(preExecutionResult).extracting(RabobankSepaSubmitPaymentPreExecutionResult::getPaymentId,
                RabobankSepaSubmitPaymentPreExecutionResult::getAuthenticationMeans,
                RabobankSepaSubmitPaymentPreExecutionResult::getRestTemplateManager,
                RabobankSepaSubmitPaymentPreExecutionResult::getPsuIpAddress,
                RabobankSepaSubmitPaymentPreExecutionResult::getSigner)
                .contains("paymentId",
                        RabobankAuthenticationMeans.fromPISAuthenticationMeans(getStatusRequest.getAuthenticationMeans()),
                        restTemplateManager,
                        "127.0.0.1",
                        signer);
    }

    @Test
    void shouldReturnPreExecutionResultWithPaymentIdTakenFromProviderStateWhenPaymentIdIsNotProvidedInRequest() {
        //given
        GetStatusRequest getStatusRequest = createGetStatusRequest(false);

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(new RabobankPaymentProviderState("paymentIdFromState"));

        //when
        RabobankSepaSubmitPaymentPreExecutionResult preExecutionResult = subject.map(getStatusRequest);

        //then
        then(providerStateDeserializer)
                .should()
                .deserialize("providerState");

        assertThat(preExecutionResult).extracting(RabobankSepaSubmitPaymentPreExecutionResult::getPaymentId,
                RabobankSepaSubmitPaymentPreExecutionResult::getAuthenticationMeans,
                RabobankSepaSubmitPaymentPreExecutionResult::getRestTemplateManager,
                RabobankSepaSubmitPaymentPreExecutionResult::getPsuIpAddress,
                RabobankSepaSubmitPaymentPreExecutionResult::getSigner)
                .contains("paymentIdFromState",
                        RabobankAuthenticationMeans.fromPISAuthenticationMeans(getStatusRequest.getAuthenticationMeans()),
                        restTemplateManager,
                        "127.0.0.1",
                        signer);
    }

    private GetStatusRequest createGetStatusRequest(boolean withPaymentId) {
        try {
            return new GetStatusRequestBuilder()
                    .setPaymentId(withPaymentId ? "paymentId" : null)
                    .setProviderState(withPaymentId ? null : "providerState")
                    .setAuthenticationMeans(sampleTypedAuthenticationMeans.getRabobankSampleTypedAuthenticationMeans())
                    .setRestTemplateManager(restTemplateManager)
                    .setPsuIpAddress("127.0.0.1")
                    .setSigner(signer)
                    .build();
        } catch (IOException | URISyntaxException e) {
            return null;
        }
    }

}
