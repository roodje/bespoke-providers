package com.yolt.providers.cbiglobe.common.pis.pec.status;

import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.model.Token;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePaymentProviderState;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePaymentProviderStateDeserializer;
import com.yolt.providers.cbiglobe.common.pis.pec.auth.CbiGlobePaymentAccessTokenProvider;
import com.yolt.providers.cbiglobe.common.pis.pec.submit.CbiGlobeSepaSubmitPreExecutionResult;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CbiGlobeStatusPaymentPreExecutionResultMapperTest {

    private CbiGlobeStatusPaymentPreExecutionResultMapper subject;

    @Mock
    private CbiGlobeBaseProperties properties;

    @Mock
    private Signer signer;

    @Mock
    private CbiGlobePaymentAccessTokenProvider paymentAccessTokenProvider;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private CbiGlobePaymentProviderStateDeserializer providerStateDeserializer;

    @BeforeEach
    void beforeEach() {
        subject = new CbiGlobeStatusPaymentPreExecutionResultMapper(paymentAccessTokenProvider,
                providerStateDeserializer, new ProviderIdentification("CBI_GLOBE",
                "CBI Globe",
                ProviderVersion.VERSION_1),
                properties);
    }

    @Test
    void shouldReturnPreExecutionResultWithPaymentIdTakenFromRequestWhenPaymentIdIsProvidedInRequest() {
        // given
        given(properties.getFirstAspspData())
                .willReturn(new AspspData());

        var authenticationMeans = new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var cbiGlobeAuthMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(authenticationMeans, "CBI_GLOBE");
        var getStatusRequest = prepareGetStatusRequest(authenticationMeans, true);
        var aspspData = properties.getFirstAspspData();
        var token = new Token();
        token.setAccessToken("accessToken");

        given(paymentAccessTokenProvider.provideClientAccessToken(restTemplateManager, cbiGlobeAuthMeans, null)).willReturn(token);

        // when
        var result = subject.map(getStatusRequest);

        // then
        assertThat(result).extracting(CbiGlobeSepaSubmitPreExecutionResult::getAuthenticationMeans,
                CbiGlobeSepaSubmitPreExecutionResult::getRestTemplateManager,
                CbiGlobeSepaSubmitPreExecutionResult::getPaymentId,
                CbiGlobeSepaSubmitPreExecutionResult::getAccessToken,
                CbiGlobeSepaSubmitPreExecutionResult::getAspspData)
                .contains(cbiGlobeAuthMeans,
                        restTemplateManager,
                        "fakePaymentId",
                        "accessToken",
                        aspspData);
        assertThat(result.getSignatureData()).isNotNull();
    }

    @Test
    void shouldReturnPreExecutionResultWithPaymentIdTakenFromProviderStateWhenPaymentIdIsNotProvidedInRequest() {
        // given
        given(properties.getFirstAspspData())
                .willReturn(new AspspData());

        var authenticationMeans = new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var cbiGlobeAuthMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(authenticationMeans, "CBI_GLOBE");
        var getStatusRequest = prepareGetStatusRequest(authenticationMeans, false);
        var aspspData = properties.getFirstAspspData();
        var token = new Token();
        token.setAccessToken("accessToken");

        given(paymentAccessTokenProvider.provideClientAccessToken(restTemplateManager, cbiGlobeAuthMeans, null)).willReturn(token);

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(new CbiGlobePaymentProviderState("paymentIdFromState"));

        // when
        var result = subject.map(getStatusRequest);

        // then
        then(providerStateDeserializer)
                .should()
                .deserialize("providerState");

        assertThat(result).extracting(CbiGlobeSepaSubmitPreExecutionResult::getAuthenticationMeans,
                CbiGlobeSepaSubmitPreExecutionResult::getRestTemplateManager,
                CbiGlobeSepaSubmitPreExecutionResult::getPaymentId,
                CbiGlobeSepaSubmitPreExecutionResult::getAccessToken,
                CbiGlobeSepaSubmitPreExecutionResult::getAspspData)
                .contains(cbiGlobeAuthMeans,
                        restTemplateManager,
                        "paymentIdFromState",
                        "accessToken",
                        aspspData);
        assertThat(result.getSignatureData()).isNotNull();
    }

    private GetStatusRequest prepareGetStatusRequest(Map<String, BasicAuthenticationMean> authenticationMeans,
                                                     boolean withPaymentId) {
        return new GetStatusRequest(withPaymentId ? null : "providerState",
                withPaymentId ? "fakePaymentId" : null,
                authenticationMeans,
                signer,
                restTemplateManager,
                "psuIpAddress",
                null
        );
    }
}