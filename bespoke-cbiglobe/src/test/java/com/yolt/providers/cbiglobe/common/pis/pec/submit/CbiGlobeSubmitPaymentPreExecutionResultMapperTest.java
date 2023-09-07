package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.model.Token;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePaymentProviderState;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePaymentProviderStateDeserializer;
import com.yolt.providers.cbiglobe.common.pis.pec.auth.CbiGlobePaymentAccessTokenProvider;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CbiGlobeSubmitPaymentPreExecutionResultMapperTest {

    private CbiGlobeSubmitPaymentPreExecutionResultMapper subject;

    @Mock
    private CbiGlobeBaseProperties properties;

    @Mock
    private Signer signer;

    @Mock
    private CbiGlobePaymentAccessTokenProvider paymentAccessTokenProvider;

    @Mock
    private CbiGlobePaymentProviderStateDeserializer providerStateDeserializer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    void beforeEach() {
        subject = new CbiGlobeSubmitPaymentPreExecutionResultMapper(paymentAccessTokenProvider,
                providerStateDeserializer, new ProviderIdentification("CBI_GLOBE",
                "CBI Globe",
                ProviderVersion.VERSION_1),
                properties);
    }

    @Test
    void shouldReturnPreExecutionResultWhenCorrectDataAreProvided() {
        // given
        given(properties.getFirstAspspData())
                .willReturn(new AspspData());

        var authenticationMeans = new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var cbiGlobeAuthenticationMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(authenticationMeans, "CBI_GLOBE");
        var submitPaymentRequest = prepareSubmitPaymentRequest(authenticationMeans, "redirectUrl");
        var aspspData = properties.getFirstAspspData();
        var token = new Token();
        token.setAccessToken("accessToken");

        given(paymentAccessTokenProvider.provideClientAccessToken(restTemplateManager, cbiGlobeAuthenticationMeans, null)).willReturn(token);

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(new CbiGlobePaymentProviderState("fakePaymentId"));

        // when
        var result = subject.map(submitPaymentRequest);

        // then
        then(providerStateDeserializer)
                .should()
                .deserialize("providerState");

        assertThat(result).extracting(CbiGlobeSepaSubmitPreExecutionResult::getAuthenticationMeans,
                CbiGlobeSepaSubmitPreExecutionResult::getRestTemplateManager,
                CbiGlobeSepaSubmitPreExecutionResult::getPaymentId,
                CbiGlobeSepaSubmitPreExecutionResult::getAccessToken,
                CbiGlobeSepaSubmitPreExecutionResult::getAspspData)
                .contains(cbiGlobeAuthenticationMeans,
                        restTemplateManager,
                        "fakePaymentId",
                        "accessToken",
                        aspspData);
        assertThat(result.getSignatureData()).isNotNull();
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenErrorInRedirectUrlPostedBackFromSite() {
        // given
        var authenticationMeans = new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var submitPaymentRequest = prepareSubmitPaymentRequest(authenticationMeans, "redirectUrl?error=error");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.map(submitPaymentRequest);

        // then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(callable)
                .withMessage("Got error in callback URL. Payment confirmation failed. Redirect url: redirectUrl?error=error");
    }

    private SubmitPaymentRequest prepareSubmitPaymentRequest(Map<String, BasicAuthenticationMean> authenticationMeans,
                                                             String redirectUrlPostedBackFromSite) {
        return new SubmitPaymentRequest(
                "providerState",
                authenticationMeans,
                redirectUrlPostedBackFromSite,
                signer,
                restTemplateManager,
                "psuIpAddress",
                null
        );
    }
}