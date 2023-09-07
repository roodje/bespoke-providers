package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class YoltBankUkInitiatePeriodicPaymentHttpHeadersProviderTest {

    private YoltBankUkInitiatePeriodicPaymentHttpHeadersProvider httpHeadersProvider;

    @BeforeEach
    public void setup() {
        httpHeadersProvider = new YoltBankUkInitiatePeriodicPaymentHttpHeadersProvider();
    }

    @Test
    void shouldReturnProperHttpHeadersForProvideHttpHeaders() {
        // given
        UUID clientId = UUID.randomUUID();
        YoltBankUkInitiatePeriodicPaymentPreExecutionResult yoltBankUkInitiatePeriodicPaymentPreExecutionResult = new YoltBankUkInitiatePeriodicPaymentPreExecutionResult(
                null,
                new PaymentAuthenticationMeans(clientId, UUID.randomUUID(), UUID.randomUUID()),
                null,
                null,
                null,
                null,
                null
        );

        // when
        HttpHeaders result = httpHeadersProvider.provideHttpHeaders(yoltBankUkInitiatePeriodicPaymentPreExecutionResult, null);

        // then
        assertThat(result.toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "client-id", clientId.toString()
                ));
    }
}