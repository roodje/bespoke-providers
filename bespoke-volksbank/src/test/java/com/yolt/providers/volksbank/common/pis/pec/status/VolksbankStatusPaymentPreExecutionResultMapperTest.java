package com.yolt.providers.volksbank.common.pis.pec.status;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPaymentProviderState;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPaymentProviderStateDeserializer;
import com.yolt.providers.volksbank.common.pis.pec.submit.VolksbankSepaSubmitPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class VolksbankStatusPaymentPreExecutionResultMapperTest {

    private VolksbankStatusPaymentPreExecutionResultMapper subject;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private VolksbankPaymentProviderStateDeserializer providerStateDeserializer;

    @BeforeEach
    void beforeEach() {
        subject = new VolksbankStatusPaymentPreExecutionResultMapper(providerStateDeserializer, new ProviderIdentification("VOLKSBANK",
                "Volksbank",
                ProviderVersion.VERSION_1));
    }

    @Test
    void shouldReturnPreExecutionResultWithPaymentIdTakenFromRequestWhenPaymentIdIsProvidedInRequest() {
        // given
        var authenticationMeans = new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var getStatusRequest = prepareGetStatusRequest(authenticationMeans, true);

        // when
        var result = subject.map(getStatusRequest);

        // then
        assertThat(result).extracting(VolksbankSepaSubmitPreExecutionResult::getAuthenticationMeans,
                VolksbankSepaSubmitPreExecutionResult::getRestTemplateManager,
                VolksbankSepaSubmitPreExecutionResult::getPaymentId)
                .contains(VolksbankAuthenticationMeans.fromAuthenticationMeans(authenticationMeans, "VOLKSBANK"),
                        restTemplateManager,
                        "fakePaymentId");
    }

    @Test
    void shouldReturnPreExecutionResultWithPaymentIdTakenFromProviderStateWhenPaymentIdIsNotProvidedInRequest() {
        // given
        var authenticationMeans = new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var getStatusRequest = prepareGetStatusRequest(authenticationMeans, false);

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(new VolksbankPaymentProviderState("paymentIdFromState"));

        // when
        var result = subject.map(getStatusRequest);

        // then
        then(providerStateDeserializer)
                .should()
                .deserialize("providerState");

        assertThat(result).extracting(VolksbankSepaSubmitPreExecutionResult::getAuthenticationMeans,
                VolksbankSepaSubmitPreExecutionResult::getRestTemplateManager,
                VolksbankSepaSubmitPreExecutionResult::getPaymentId)
                .contains(VolksbankAuthenticationMeans.fromAuthenticationMeans(authenticationMeans, "VOLKSBANK"),
                        restTemplateManager,
                        "paymentIdFromState");
    }

    private GetStatusRequest prepareGetStatusRequest(Map<String, BasicAuthenticationMean> authenticationMeans,
                                                     boolean withPaymentId) {
        return new GetStatusRequest(withPaymentId ? null : "providerState",
                withPaymentId ? "fakePaymentId" : null,
                authenticationMeans,
                null,
                restTemplateManager,
                "",
                null
        );
    }
}