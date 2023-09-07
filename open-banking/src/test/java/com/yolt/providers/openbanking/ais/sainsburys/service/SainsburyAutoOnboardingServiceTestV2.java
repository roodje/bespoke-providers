package com.yolt.providers.openbanking.ais.sainsburys.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.sainsburys.SainsburysPropertiesV2;
import com.yolt.providers.openbanking.ais.sainsburys.SainsburysSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.sainsburys.service.ais.restclient.SainsburysRestClientV2;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.yolt.providers.openbanking.ais.sainsburys.auth.SainsburysAuthMeansMapperV2.SOFTWARE_ID_NAME;
import static com.yolt.providers.openbanking.ais.sainsburys.auth.SainsburysAuthMeansMapperV2.SOFTWARE_STATEMENT_ASSERTION_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class SainsburyAutoOnboardingServiceTestV2 {

    private static final String ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final String PRIVATE_KEY_JWT = "private_key_jwt";
    private static final String AUDIENCE = "test-audience";

    @Mock
    private SainsburysRestClientV2 restClient;

    @Mock
    private AuthenticationService authenticationService;

    private final SainsburysAutoOnboardingServiceV2 subject = new SainsburysAutoOnboardingServiceV2(
            restClient,
            AlgorithmIdentifiers.RSA_PSS_USING_SHA256,
            createTestProperties(),
            authenticationService,
            com.yolt.providers.openbanking.ais.generic2.domain.TokenScope.builder()
                    .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                    .build());

    @Test
    void shouldCreateProperJws() throws Exception {
        //Given
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setRedirectUrls(Collections.singletonList("https://www.test-url.com/"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        Map<String, BasicAuthenticationMean> authenticationMeans = SainsburysSampleTypedAuthMeansV2.getAuthenticationMeans();

        //When
        JsonWebSignature jws = subject.createJws(authenticationMeans, urlAutoOnboardingRequest);

        //Then
        assertThat(jws.getAlgorithm().getAlgorithmIdentifier()).isEqualTo(ALGORITHM);
        DocumentContext jsonContext = JsonPath.parse(jws.getUnverifiedPayload());
        assertThat(jsonContext.read("$['aud']").toString()).isEqualTo(AUDIENCE);
        assertThat(jsonContext.read("$['iss']").toString()).isEqualTo(authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        assertThat(jsonContext.read("$['software_id']").toString()).isEqualTo(authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        assertThat(jsonContext.read("$['software_statement']").toString()).isEqualTo(authenticationMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        assertThat(jsonContext.read("$['token_endpoint_auth_method']").toString()).isEqualTo(PRIVATE_KEY_JWT);
        assertThat(jsonContext.read("$['id_token_signing_alg']").toString()).isEqualTo(ALGORITHM);
        assertThat(jsonContext.read("$['request_object_signing_alg']").toString()).isEqualTo(ALGORITHM);
        assertThat(jsonContext.read("$['token_endpoint_auth_signing_alg']").toString()).isEqualTo(ALGORITHM);
        assertThat(jsonContext.read("$['id_token_signed_response_alg']").toString()).isEqualTo(ALGORITHM);
    }

    private SainsburysPropertiesV2 createTestProperties() {
        SainsburysPropertiesV2 properties = new SainsburysPropertiesV2();
        properties.setAudience(AUDIENCE);
        return properties;
    }
}