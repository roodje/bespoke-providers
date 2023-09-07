package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class YoltBankUkInitiateSinglePaymentHttpHeadersProviderTest {

    private YoltBankUkInitiateSinglePaymentHttpHeadersProvider httpHeadersProvider;

    @BeforeEach
    public void setup() {
        httpHeadersProvider = new YoltBankUkInitiateSinglePaymentHttpHeadersProvider();
    }

    @Test
    void shouldReturnProperHttpHeadersForProvideHttpHeaders() {
        // given
        UUID clientId = UUID.randomUUID();
        YoltBankUkInitiateSinglePaymentPreExecutionResult yoltBankUkInitiateSinglePaymentPreExecutionResult = new YoltBankUkInitiateSinglePaymentPreExecutionResult(
                null,
                new PaymentAuthenticationMeans(clientId, UUID.randomUUID(), UUID.randomUUID()),
                null,
                null,
                null,
                null,
                null
        );

        // when
        HttpHeaders result = httpHeadersProvider.provideHttpHeaders(yoltBankUkInitiateSinglePaymentPreExecutionResult, null);

        // then
        assertThat(result.toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "client-id", clientId.toString()
                ));
    }
}