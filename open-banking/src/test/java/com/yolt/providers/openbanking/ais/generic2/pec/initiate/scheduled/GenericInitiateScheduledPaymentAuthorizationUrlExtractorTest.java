package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenericInitiateScheduledPaymentAuthorizationUrlExtractorTest {

    @InjectMocks
    private GenericInitiateScheduledPaymentAuthorizationUrlExtractor subject;

    @Mock
    private TokenScope tokenScope;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private Signer signer;

    @Test
    void shouldReturnAuthorizationUrlWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticScheduledConsentResponse5 obWriteDomesticScheduledConsentResponse5 = new OBWriteDomesticScheduledConsentResponse5();
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder()
                .clientId("clientId")
                .build();
        GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult = new GenericInitiateScheduledPaymentPreExecutionResult();
        preExecutionResult.setExternalPaymentId("externalPaymentId");
        preExecutionResult.setSigner(signer);
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setState("state");
        preExecutionResult.setBaseClientRedirectUrl("baseClientRedirectUrl");

        given(authenticationService.generateAuthorizationUrl(authMeans, "externalPaymentId", "state", "baseClientRedirectUrl", tokenScope, signer))
                .willReturn("authUrl");

        // when
        String result = subject.extractAuthorizationUrl(obWriteDomesticScheduledConsentResponse5, preExecutionResult);

        // then
        assertThat(result).isEqualTo("authUrl");
    }

    @Test
    void shouldGetResourceIdFromResponseWhenExternalPaymentIdIsNotProvidedInPreExecutionResult() {
        // given
        OBWriteDomesticScheduledConsentResponse5 obWriteDomesticScheduledConsentResponse5 = new OBWriteDomesticScheduledConsentResponse5()
                .data(new OBWriteDomesticScheduledConsentResponse5Data()
                        .consentId("consentId"));
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder()
                .build();
        GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult = new GenericInitiateScheduledPaymentPreExecutionResult();
        preExecutionResult.setSigner(signer);
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setState("state");
        preExecutionResult.setBaseClientRedirectUrl("baseClientRedirectUrl");

        given(authenticationService.generateAuthorizationUrl(authMeans, "consentId", "state", "baseClientRedirectUrl", tokenScope, signer))
                .willReturn("authUrl");

        // when
        String result = subject.extractAuthorizationUrl(obWriteDomesticScheduledConsentResponse5, preExecutionResult);

        // then
        assertThat(result).isEqualTo("authUrl");
    }
}