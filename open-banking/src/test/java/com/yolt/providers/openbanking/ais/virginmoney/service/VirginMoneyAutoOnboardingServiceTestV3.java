package com.yolt.providers.openbanking.ais.virginmoney.service;


import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneySampleAuthenticationMeansV3;
import com.yolt.providers.openbanking.ais.virginmoney.config.VirginMoneyPropertiesV2;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.yolt.providers.openbanking.ais.virginmoney.auth.VirginMoneyAuthMeansBuilderV4.SOFTWARE_ID_NAME;
import static com.yolt.providers.openbanking.ais.virginmoney.auth.VirginMoneyAuthMeansBuilderV4.SOFTWARE_STATEMENT_ASSERTION_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class VirginMoneyAutoOnboardingServiceTestV3 {

    private static final String ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final String PRIVATE_KEY_JWT = "tls_client_auth";


    private VirginMoneySampleAuthenticationMeansV3 sampleAuthenticationMeansFixture = new VirginMoneySampleAuthenticationMeansV3();

    @Test
    void shouldCreateProperJws() throws Exception {
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setRedirectUrls(Collections.singletonList("https://www.yolt.com/calback-test"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        String audience = "test-audience";
        Map<String, BasicAuthenticationMean> authenticationMeans = sampleAuthenticationMeansFixture.getVirginMoneySampleAuthenticationMeansForAutoonboarding();
        VirginMoneyPropertiesV2 properties = new VirginMoneyPropertiesV2();
        properties.setRegistrationAudience(audience);
        VirginMoneyAutoOnboardingServiceV3 subject = new VirginMoneyAutoOnboardingServiceV3(properties);

        //When
        JsonWebSignature jws = subject.createJws(authenticationMeans, urlAutoOnboardingRequest);

        //Then
        assertThat(jws.getAlgorithm().getAlgorithmIdentifier()).isEqualTo(ALGORITHM);
        DocumentContext jsonContext = JsonPath.parse(jws.getUnverifiedPayload());
        assertThat(jsonContext.read("$['aud']").toString()).isEqualTo(audience);
        assertThat(jsonContext.read("$['iss']").toString()).isEqualTo(authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        assertThat(jsonContext.read("$['software_id']").toString()).isEqualTo(authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        assertThat(jsonContext.read("$['software_statement']").toString()).isEqualTo(authenticationMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        assertThat(jsonContext.read("$['token_endpoint_auth_method']").toString()).isEqualTo(PRIVATE_KEY_JWT);
        assertThat(jsonContext.read("$['id_token_signing_alg']").toString()).isEqualTo(ALGORITHM);
        assertThat(jsonContext.read("$['request_object_signing_alg']").toString()).isEqualTo(ALGORITHM);
        assertThat(jsonContext.read("$['token_endpoint_auth_signing_alg']").toString()).isEqualTo(ALGORITHM);
        assertThat(jsonContext.read("$['id_token_signed_response_alg']").toString()).isEqualTo(ALGORITHM);
        assertThat(jsonContext.read("$['scope']").toString()).isEqualTo("openid accounts payments");
        assertThat(jsonContext.read("$['redirect_uris']").toString()).isEqualTo("[\"https:\\/\\/www.yolt.com\\/calback-test\"]");
    }
}