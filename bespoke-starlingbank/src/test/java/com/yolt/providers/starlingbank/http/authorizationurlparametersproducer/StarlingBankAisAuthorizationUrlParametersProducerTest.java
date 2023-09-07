package com.yolt.providers.starlingbank.http.authorizationurlparametersproducer;

import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.http.authorizationurlparametersproducer.StarlingBankAisAuthorizationUrlParametersProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StarlingBankAisAuthorizationUrlParametersProducerTest {

    private static final String API_KEY = "example-api-key";
    private static final String REDIRECT_URL = "https://example.yolt.com";
    private static final String LOGIN_STATE = "example-login-state";
    private static final String ACCOUNTS_SCOPE = "account:read%20account-holder-name:read%20account-holder-type:read%20account-identifier:read%20account-list:read%20balance:read%20savings-goal:read%20savings-goal-transfer:read%20transaction:read";

    @InjectMocks
    private StarlingBankAisAuthorizationUrlParametersProducer authorizationUrlParametersProducer;

    @Test
    void shouldReturnValidListOfAuthorizationUrlParametersForCorrectData() {
        // given
        StarlingBankAuthenticationMeans authenticationMeans = StarlingBankAuthenticationMeans.builder()
                .apiKey(API_KEY)
                .build();

        // when
        MultiValueMap<String, String> aisAuthorizationUrlParametersList = authorizationUrlParametersProducer
                .createAuthorizationUrlParameters(LOGIN_STATE, REDIRECT_URL, authenticationMeans);

        // then
        assertThat(aisAuthorizationUrlParametersList).containsOnlyKeys(
                RESPONSE_TYPE,
                CLIENT_ID,
                REDIRECT_URI,
                STATE,
                SCOPE
        );
        assertThat(aisAuthorizationUrlParametersList.toSingleValueMap().get(SCOPE)).isEqualTo(ACCOUNTS_SCOPE);
    }
}