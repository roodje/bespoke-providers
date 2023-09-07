package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.pis.common.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;

class YoltBankUkSubmitPaymentHttpHeadersProviderTest {

    private YoltBankUkSubmitPaymentHttpHeadersProvider subject;

    @BeforeEach
    public void setup() {
        subject = new YoltBankUkSubmitPaymentHttpHeadersProvider();
    }

    @Test
    void shouldReturnProperHttpHeadersForProvideHttpHeadersWhenCorrectData() {
        // given
        UUID clientId = UUID.randomUUID();
        YoltBankUkSubmitPreExecutionResult yoltBankUkSubmitPreExecutionResult = new YoltBankUkSubmitPreExecutionResult(
                null,
                clientId,
                SINGLE
        );

        // when
        HttpHeaders result = subject.provideHttpHeaders(yoltBankUkSubmitPreExecutionResult, null);

        // then
        assertThat(result.toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "client-id", clientId.toString()
                ));
    }
}