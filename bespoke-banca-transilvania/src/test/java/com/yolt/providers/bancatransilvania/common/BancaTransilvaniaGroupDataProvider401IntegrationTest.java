package com.yolt.providers.bancatransilvania.common;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test class contains a test when the bank's API returns HTTP-401 status code on data endpoint.
 * This means that request is unauthorized, thus we want to force user to fill a consent (so throw {@link TokenInvalidException})
 */
@AutoConfigureWireMock(stubs = "classpath:/stubs/unhappy-flow/accounts-401/", httpsPort = 0, port = 0)
class BancaTransilvaniaGroupDataProvider401IntegrationTest extends BancaTransilvaniaGroupDataProviderTestBaseSetup {

    @Test
    void shouldHandleUnauthorizedCorrectly() {
        final UrlFetchDataRequest request = buildGenericFetchDataRequest();

        final ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(request);

        assertThatThrownBy(fetchDataCallable)
                .isInstanceOfSatisfying(TokenInvalidException.class, exception -> assertThat(exception.getMessage()).isEqualTo("We are not authorized to call endpoint: HTTP 401"));
    }
}