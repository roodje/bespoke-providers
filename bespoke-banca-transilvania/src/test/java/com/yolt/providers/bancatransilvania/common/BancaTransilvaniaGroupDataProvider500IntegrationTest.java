package com.yolt.providers.bancatransilvania.common;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.web.client.HttpServerErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test class contains a test when the bank's API returns HTTP-500 status code on data endpoint
 * This means that e.g. there is a server error (so throw {@link ProviderFetchDataException})
 */
@AutoConfigureWireMock(stubs = "classpath:/stubs/unhappy-flow/accounts-500/", httpsPort = 0, port = 0)
class BancaTransilvaniaGroupDataProvider500IntegrationTest extends BancaTransilvaniaGroupDataProviderTestBaseSetup {

    @Test
    void shouldHandleServerErrorProperly() {
        final UrlFetchDataRequest request = buildGenericFetchDataRequest();

        final ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(request);

        assertThatThrownBy(fetchDataCallable)
                .isInstanceOfSatisfying(ProviderFetchDataException.class, exception -> {
                    final Throwable cause = exception.getCause();
                    assertThat(cause).isInstanceOf(ProviderHttpStatusException.class);
                    assertThat(cause.getCause()).isInstanceOf(HttpServerErrorException.class);
                    assertThat(cause.getMessage()).isEqualTo("Something went wrong on bank side: HTTP 500");
                });
    }
}