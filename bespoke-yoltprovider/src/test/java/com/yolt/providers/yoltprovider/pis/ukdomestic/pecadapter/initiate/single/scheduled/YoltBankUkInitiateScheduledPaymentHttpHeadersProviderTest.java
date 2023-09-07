package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class YoltBankUkInitiateScheduledPaymentHttpHeadersProviderTest {

    private YoltBankUkInitiateScheduledPaymentHttpHeadersProvider httpHeadersProvider;

    @BeforeEach
    public void setup() {
        httpHeadersProvider = new YoltBankUkInitiateScheduledPaymentHttpHeadersProvider();
    }

    @Test
    void shouldReturnProperHttpHeadersForProvideHttpHeaders() {
        // given
        UUID clientId = UUID.randomUUID();
        YoltBankUkInitiateScheduledPaymentPreExecutionResult yoltBankUkInitiateScheduledPaymentPreExecutionResult = new YoltBankUkInitiateScheduledPaymentPreExecutionResult(
                null,
                new PaymentAuthenticationMeans(clientId, UUID.randomUUID(), UUID.randomUUID()),
                null,
                null,
                null,
                null,
                null
        );

        // when
        HttpHeaders result = httpHeadersProvider.provideHttpHeaders(yoltBankUkInitiateScheduledPaymentPreExecutionResult, null);

        // then
        assertThat(result.toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "client-id", clientId.toString()
                ));
    }
}