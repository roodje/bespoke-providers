package com.yolt.providers.bancatransilvania.common;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test class contains a test when the bank's API returns HTTP-400 status code on data endpoint.
 * This means that e.g. there is a mistake in our request, thus we can't map such account (so throw {@link ProviderFetchDataException})
 */
@AutoConfigureWireMock(stubs = "classpath:/stubs/unhappy-flow/accounts-400/", httpsPort = 0, port = 0)
class BancaTransilvaniaGroupDataProvider400IntegrationTest extends BancaTransilvaniaGroupDataProviderTestBaseSetup {

    @Test
    void shouldHandleBadRequestCorrectly() {
        final UrlFetchDataRequest request = buildGenericFetchDataRequest();

        final ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(request);

        assertThatThrownBy(fetchDataCallable)
                .isInstanceOfSatisfying(ProviderFetchDataException.class, exception -> {
                    final Throwable cause = exception.getCause();
                    assertThat(cause).isInstanceOf(ProviderHttpStatusException.class);
                    assertThat(cause.getCause()).isInstanceOf(HttpClientErrorException.class);
                    assertThat(cause.getMessage()).isEqualTo("Request formed incorrectly: HTTP 400");
                });
    }
}