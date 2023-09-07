package com.yolt.providers.axabanque.common.fetchdata.http;

import com.yolt.providers.axabanque.common.fetchdata.errorhandler.DefaultFetchDataHttpErrorHandlerV2;
import com.yolt.providers.axabanque.common.fetchdata.http.client.DefaultFetchDataHttpClientV2;
import com.yolt.providers.axabanque.common.fetchdata.http.headerproducer.FetchDataRequestHeadersProducer;
import com.yolt.providers.axabanque.common.model.external.Accounts;
import com.yolt.providers.axabanque.common.model.external.Transactions;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultFetchDataHttpClientV2Test {

    private MeterRegistry registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
    @Mock
    private FetchDataRequestHeadersProducer headersProducer;
    @Mock
    private RestTemplate restTemplate;
    private DefaultFetchDataHttpClientV2 client;

    @BeforeEach
    public void setup() {
        client = new DefaultFetchDataHttpClientV2(registry, restTemplate, "AXA", "v1", headersProducer, DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC), new DefaultFetchDataHttpErrorHandlerV2(new DefaultHttpErrorHandlerV2()));
    }

    @Test
    void shouldSendGetAccountsRequest() throws TokenInvalidException {
        //given
        HttpHeaders headers = HttpHeaders.EMPTY;
        Accounts expectedAccounts = mock(Accounts.class);
        when(headersProducer.getAccountsHeaders("accessToken", "consentId", "xRequestId", "127.0.1.3"))
                .thenReturn(headers);
        when(restTemplate.exchange("/{version}/accounts?withBalance=true", HttpMethod.GET, new HttpEntity<>(headers), Accounts.class, "v1"))
                .thenReturn(new ResponseEntity<Accounts>(expectedAccounts, HttpStatus.OK));
        //when
        Accounts response = client.getAccounts("accessToken", "consentId", "xRequestId", "127.0.1.3");
        //then
        assertThat(response).isEqualTo(expectedAccounts);
    }


    @Test
    void shouldSendGetTransactionsRequest() throws TokenInvalidException {
        //given
        HttpHeaders headers = new HttpHeaders();
        Instant dummyDate = Instant.parse("1815-06-18T18:35:24.00Z");
        Transactions expectedTransactions = mock(Transactions.class);
        when(headersProducer.getTransactionsHeaders("accessToken", "consentId", "xRequestId", "127.0.1.5"))
                .thenReturn(headers);
        when(restTemplate.exchange("/{version}/accounts/{accountId}/transactions?withBalance=false&dateFrom=1815-06-18&bookingStatus=both",
                HttpMethod.GET, new HttpEntity<>(headers), Transactions.class, "v1", "123"))
                .thenReturn(new ResponseEntity<>(expectedTransactions, HttpStatus.OK));
        //when
        Transactions response = client.getTransactions("123", dummyDate, "accessToken", "consentId", "/{version}/accounts/{accountId}/transactions", "xRequestId", 0, "127.0.1.5");
        //then
        assertThat(response).isEqualTo(expectedTransactions);
    }
}
