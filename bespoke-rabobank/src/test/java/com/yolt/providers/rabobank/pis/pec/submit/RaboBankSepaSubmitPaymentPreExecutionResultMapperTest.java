package com.yolt.providers.rabobank.pis.pec.submit;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.RabobankSampleTypedAuthenticationMeans;
import com.yolt.providers.rabobank.pis.pec.RabobankPaymentProviderState;
import com.yolt.providers.rabobank.pis.pec.RabobankPaymentProviderStateDeserializer;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RaboBankSepaSubmitPaymentPreExecutionResultMapperTest {

    @InjectMocks
    private RaboBankSepaSubmitPaymentPreExecutionResultMapper subject;

    private final RabobankSampleTypedAuthenticationMeans sampleTypedAuthenticationMeans = new RabobankSampleTypedAuthenticationMeans();

    @Mock
    private RabobankPaymentProviderStateDeserializer providerStateDeserializer;

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Test
    void shouldReturnedPreExecutionResultWhenDataAreCorrect() throws IOException, URISyntaxException {
        //given
        Map<String, BasicAuthenticationMean> typedAuthenticationMeansMap = sampleTypedAuthenticationMeans.getRabobankSampleTypedAuthenticationMeans();
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest("someProviderState",
                typedAuthenticationMeansMap,
                "https://redirectUrl.com/postedFromSite",
                signer,
                restTemplateManager,
                "127.0.0.1",
                null);
        RabobankSepaSubmitPaymentPreExecutionResult expectedPreExecutionResponse = new RabobankSepaSubmitPaymentPreExecutionResult(
                "b20d4f18-b937-11eb-8529-0242ac130003",
                RabobankAuthenticationMeans.fromPISAuthenticationMeans(typedAuthenticationMeansMap),
                restTemplateManager,
                "127.0.0.1",
                signer);

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(new RabobankPaymentProviderState("b20d4f18-b937-11eb-8529-0242ac130003"));

        //when
        RabobankSepaSubmitPaymentPreExecutionResult result = subject.map(submitPaymentRequest);

        //then
        then(providerStateDeserializer)
                .should()
                .deserialize("someProviderState");

        assertThat(result).isEqualTo(expectedPreExecutionResponse);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenReceivedErrorInRedirectUrl() throws IOException, URISyntaxException {
        //given
        Map<String, BasicAuthenticationMean> typedAuthenticationMeansMap = sampleTypedAuthenticationMeans.getRabobankSampleTypedAuthenticationMeans();
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest("someProviderState",
                typedAuthenticationMeansMap,
                "https://redirectUrl.com/postedFromSite?error=access-denied",
                signer,
                restTemplateManager,
                "127.0.0.1",
                null);

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(new RabobankPaymentProviderState("b20d4f18-b937-11eb-8529-0242ac130003"));

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.map(submitPaymentRequest);

        //then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(call)
                .withMessage("Got error in callback URL. Payment confirmation failed. Redirect url: https://redirectUrl.com/postedFromSite?error=access-denied");

    }
}
