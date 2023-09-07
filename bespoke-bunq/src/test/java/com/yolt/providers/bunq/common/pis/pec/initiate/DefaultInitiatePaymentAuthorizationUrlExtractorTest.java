package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.service.authorization.BunqAuthorizationServiceV5;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentAuthorizationUrlExtractorTest {

    @Mock
    BunqAuthorizationServiceV5 authorizationService;

    private DefaultInitiatePaymentAuthorizationUrlExtractor authorizationUrlExtractor;

    @BeforeEach
    void setUp() {
        authorizationUrlExtractor = new DefaultInitiatePaymentAuthorizationUrlExtractor(authorizationService);
    }

    @Test
    void shouldReturnExtractedAuthorizationUrlWhenCorrectDataAreProvided() {
        //given
        BunqAuthenticationMeansV2 bunqAuthenticationMeansV2 = new BunqAuthenticationMeansV2("clientId", "clientSecret", 1L, 1L, null);
        DefaultInitiatePaymentPreExecutionResult preExecutionResult = new DefaultInitiatePaymentPreExecutionResult(
                null, null, "http://yolt.com/callback", "someState",
                bunqAuthenticationMeansV2.getClientId(), bunqAuthenticationMeansV2.getPsd2UserId(), null, 1L, null);
        String expectedLoginUrl = "https://generatedauthorizationurl.com/";
        when(authorizationService.getLoginUrl(bunqAuthenticationMeansV2.getClientId(), "http://yolt.com/callback", "someState")).thenReturn(expectedLoginUrl);

        //when
        String result = authorizationUrlExtractor.extractAuthorizationUrl(null, preExecutionResult);

        //then
        assertThat(result).isEqualTo(expectedLoginUrl);
    }
}