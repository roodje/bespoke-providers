package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPaymentProviderState;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPaymentProviderStateDeserializer;
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
class VolksbankSubmitPaymentPreExecutionResultMapperTest {

    private VolksbankSubmitPaymentPreExecutionResultMapper subject;

    @Mock
    private VolksbankPaymentProviderStateDeserializer providerStateDeserializer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    void beforeEach() {
        subject = new VolksbankSubmitPaymentPreExecutionResultMapper(
                providerStateDeserializer,
                new ProviderIdentification("VOLKSBANK",
                        "Volksbank",
                        ProviderVersion.VERSION_1));
    }

    @Test
    void shouldReturnPreExecutionResultWhenCorrectDataAreProvided() {
        // given
        var basicAuthMeans = new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var authenticationMeans = VolksbankAuthenticationMeans.fromAuthenticationMeans(basicAuthMeans, "VOLKSBANK");
        var submitPaymentRequest = prepareSubmitPaymentRequest(basicAuthMeans);

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(new VolksbankPaymentProviderState("fakePaymentId"));

        // when
        var result = subject.map(submitPaymentRequest);

        // then
        then(providerStateDeserializer)
                .should()
                .deserialize("fakeProviderState");

        assertThat(result).extracting(VolksbankSepaSubmitPreExecutionResult::getAuthenticationMeans,
                VolksbankSepaSubmitPreExecutionResult::getRestTemplateManager,
                VolksbankSepaSubmitPreExecutionResult::getPaymentId)
                .contains(authenticationMeans,
                        restTemplateManager,
                        "fakePaymentId");
    }

    private SubmitPaymentRequest prepareSubmitPaymentRequest(Map<String, BasicAuthenticationMean> basicAuthMeans) {
        return new SubmitPaymentRequest(
                "fakeProviderState",
                basicAuthMeans,
                "redirectUrl",
                null,
                restTemplateManager,
                "",
                null
        );
    }
}