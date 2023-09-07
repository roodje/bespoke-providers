package com.yolt.providers.bancatransilvania.common;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.BackPressureRequestException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test class contains a test when the bank's API returns HTTP-429 status code on data endpoint
 * This means that there are too many requests. We throw {@link BackPressureRequestException} in such case
 */
@AutoConfigureWireMock(stubs = "classpath:/stubs/unhappy-flow/accounts-429/", httpsPort = 0, port = 0)
class BancaTransilvaniaGroupDataProvider429IntegrationTest extends BancaTransilvaniaGroupDataProviderTestBaseSetup {

    @Test
    void shouldThrowBackPressureRequestExceptionWhenAccountsRequestFailDueToTooManyRequestsError() {
        final UrlFetchDataRequest request = buildGenericFetchDataRequest();

        final ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(request);

        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(BackPressureRequestException.class);
    }
}