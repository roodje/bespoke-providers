package com.yolt.providers.stet.generic.service.fetchData.account;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.dto.TestStetAccountDTO;
import com.yolt.providers.stet.generic.dto.TestStetAccountsResponseDTO;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountsResponseDTO;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchBasicAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FetchBasicAccountsStrategyTest {

    private static final String BASE_URL = "http://localhost/";
    private static final String ACCESS_TOKEN = "9ab42e62-247f-4b94-8ae7-7157874033d9";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String ACCOUNTS_ENDPOINT = "/accounts";
    private static final String CONSENTS_ENDPOINT = "/consents";

    @InjectMocks
    private FetchBasicAccountsStrategy fetchBasicAccountsStrategy;

    @Mock
    private FetchDataRestClient restClient;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Signer signer;

    @Captor
    private ArgumentCaptor<DataRequest> dataRequestArgumentCaptor;

    @Test
    void shouldReturnAccountsAfterFetchingThem() throws TokenInvalidException {
        // given
        DataRequest dataRequest = createDataRequest();
        StetAccountsResponseDTO expectedAccountsResponseDTO = createStetAccountsResponseDTO();

        when(restClient.getAccounts(any(HttpClient.class), anyString(), any(DataRequest.class)))
                .thenReturn(expectedAccountsResponseDTO);

        // when
        List<StetAccountDTO> result = fetchBasicAccountsStrategy.fetchAccounts(httpClient, ACCOUNTS_ENDPOINT, CONSENTS_ENDPOINT, dataRequest);

        // then
        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedAccountsResponseDTO.getAccounts());
        verify(restClient).getAccounts(eq(httpClient), eq(ACCOUNTS_ENDPOINT), dataRequestArgumentCaptor.capture());
        assertThat(dataRequestArgumentCaptor.getValue()).isEqualTo(dataRequest);
    }

    @Test
    void shouldReturnNoAccountsAfterFetchingThemWhenResponseIsNull() throws TokenInvalidException {
        // given
        DataRequest dataRequest = createDataRequest();
        StetAccountsResponseDTO expectedAccountsResponseDTO = createStetAccountsResponseDTO();

        when(restClient.getAccounts(any(HttpClient.class), anyString(), any(DataRequest.class)))
                .thenReturn(expectedAccountsResponseDTO);

        // when
        List<StetAccountDTO> accountDTOs = fetchBasicAccountsStrategy.fetchAccounts(httpClient, ACCOUNTS_ENDPOINT, CONSENTS_ENDPOINT, dataRequest);

        // then
        assertThat(accountDTOs).containsAll(expectedAccountsResponseDTO.getAccounts());
        verify(restClient).getAccounts(eq(httpClient), eq(ACCOUNTS_ENDPOINT), dataRequestArgumentCaptor.capture());
        assertThat(dataRequestArgumentCaptor.getValue()).isEqualTo(dataRequest);
    }

    private DataRequest createDataRequest() {
        DefaultAuthenticationMeans authMeans = DefaultAuthenticationMeans.builder().build();
        return new DataRequest(BASE_URL, signer, authMeans, ACCESS_TOKEN, PSU_IP_ADDRESS, false);
    }

    private StetAccountsResponseDTO createStetAccountsResponseDTO() {
        return TestStetAccountsResponseDTO.builder()
                .accounts(Collections.singletonList(TestStetAccountDTO.builder()
                        .name("AccountName")
                        .build()))
                .build();
    }
}
