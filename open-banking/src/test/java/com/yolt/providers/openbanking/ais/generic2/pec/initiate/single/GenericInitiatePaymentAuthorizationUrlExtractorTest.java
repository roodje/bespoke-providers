package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericInitiatePaymentAuthorizationUrlExtractorTest {

    @InjectMocks
    private GenericInitiatePaymentAuthorizationUrlExtractor subject;

    @Mock
    private TokenScope tokenScope;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private Signer signer;

    @Test
    void shouldReturnAuthorizationUrlWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 = new OBWriteDomesticConsentResponse5();
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder()
                .clientId("clientId")
                .build();
        GenericInitiatePaymentPreExecutionResult preExecutionResult = new GenericInitiatePaymentPreExecutionResult();
        preExecutionResult.setExternalPaymentId("externalPaymentId");
        preExecutionResult.setSigner(signer);
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setState("state");
        preExecutionResult.setBaseClientRedirectUrl("baseClientRedirectUrl");

        given(authenticationService.generateAuthorizationUrl(any(DefaultAuthMeans.class), anyString(), anyString(), anyString(), any(TokenScope.class), any(Signer.class)))
                .willReturn("authUrl");

        // when
        String result = subject.extractAuthorizationUrl(obWriteDomesticConsentResponse5, preExecutionResult);

        // then
        then(authenticationService)
                .should()
                .generateAuthorizationUrl(authMeans, "externalPaymentId", "state", "baseClientRedirectUrl", tokenScope, signer);

        assertThat(result).isEqualTo("authUrl");
    }

    @Test
    void shouldGetResourceIdFromResponseWhenExternalPaymentIdIsNotProvidedInPreExecutionResult() {
        // given
        OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 = new OBWriteDomesticConsentResponse5()
                .data(new OBWriteDomesticConsentResponse5Data()
                        .consentId("consentId"));
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder()
                .build();
        GenericInitiatePaymentPreExecutionResult preExecutionResult = new GenericInitiatePaymentPreExecutionResult();
        preExecutionResult.setSigner(signer);
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setState("state");
        preExecutionResult.setBaseClientRedirectUrl("baseClientRedirectUrl");

        given(authenticationService.generateAuthorizationUrl(any(DefaultAuthMeans.class), anyString(), anyString(), anyString(), any(TokenScope.class), any(Signer.class)))
                .willReturn("authUrl");

        // when
        String result = subject.extractAuthorizationUrl(obWriteDomesticConsentResponse5, preExecutionResult);

        // then
        then(authenticationService)
                .should()
                .generateAuthorizationUrl(authMeans, "consentId", "state", "baseClientRedirectUrl", tokenScope, signer);

        assertThat(result).isEqualTo("authUrl");
    }
}