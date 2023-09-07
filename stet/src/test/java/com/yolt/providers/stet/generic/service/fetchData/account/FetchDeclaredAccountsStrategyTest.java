package com.yolt.providers.stet.generic.service.fetchData.account;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.TestStetAccountDTO;
import com.yolt.providers.stet.generic.dto.TestStetAccountsResponseDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountsResponseDTO;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchDeclaredAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FetchDeclaredAccountsStrategyTest {

    private static final String BASE_URL = "http://localhost/";
    private static final String ACCESS_TOKEN = "9ab42e62-247f-4b94-8ae7-7157874033d9";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String ACCOUNTS_ENDPOINT = "/accounts";
    private static final String CONSENTS_ENDPOINT = "/consents";

    @Mock
    private FetchDataRestClient restClient;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Signer signer;

    @Captor
    private ArgumentCaptor<DataRequest> dataRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> updateConsentBodyArgumentCaptor;

    @InjectMocks
    private FetchDeclaredAccountsStrategy fetchDeclaredAccountsStrategy;

    @Test
    void shouldReturnAccountsAfterFetchingThemTwiceWithConsentUpdate() throws TokenInvalidException {
        // given
        DataRequest dataRequest = createDataRequest();
        StetAccountsResponseDTO expectedAccountsResponseDTO = createStetAccountsResponseDTO(createStetAccountDTOs());
        Map<String, Object> expectedUpdateConsentBody = createUpdateConsentBody(expectedAccountsResponseDTO);

        when(restClient.getAccounts(any(HttpClient.class), anyString(), any(DataRequest.class)))
                .thenReturn(expectedAccountsResponseDTO);
        when(restClient.updateConsent(any(HttpClient.class), anyString(), any(DataRequest.class), anyMap()))
                .thenReturn(ResponseEntity.of(Optional.empty()));

        // when
        List<StetAccountDTO> result = fetchDeclaredAccountsStrategy.fetchAccounts(httpClient, ACCOUNTS_ENDPOINT, CONSENTS_ENDPOINT, dataRequest);

        // then
        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedAccountsResponseDTO.getAccounts());
        verify(restClient, times(2)).getAccounts(eq(httpClient), eq(ACCOUNTS_ENDPOINT), dataRequestArgumentCaptor.capture());
        verify(restClient).updateConsent(eq(httpClient), eq(CONSENTS_ENDPOINT), dataRequestArgumentCaptor.capture(), updateConsentBodyArgumentCaptor.capture());
        assertThat(dataRequestArgumentCaptor.getValue()).isEqualTo(dataRequest);
        assertThat(updateConsentBodyArgumentCaptor.getValue()).isEqualTo(expectedUpdateConsentBody);
    }

    @Test
    void shouldReturnNoAccountsAfterFetchingThemWhenResponseIsNull() throws TokenInvalidException {
        // given
        DataRequest dataRequest = createDataRequest();
        StetAccountsResponseDTO expectedAccountsResponseDTO = createStetAccountsResponseDTO(Collections.emptyList());

        when(restClient.getAccounts(any(HttpClient.class), anyString(), any(DataRequest.class)))
                .thenReturn(expectedAccountsResponseDTO);

        // when
        List<StetAccountDTO> accountDTOs = fetchDeclaredAccountsStrategy.fetchAccounts(httpClient, ACCOUNTS_ENDPOINT, CONSENTS_ENDPOINT, dataRequest);

        // then
        assertThat(accountDTOs)
                .hasSameSizeAs(expectedAccountsResponseDTO.getAccounts())
                .isEmpty();

        verify(restClient).getAccounts(eq(httpClient), eq(ACCOUNTS_ENDPOINT), dataRequestArgumentCaptor.capture());
        assertThat(dataRequestArgumentCaptor.getValue()).isEqualTo(dataRequest);
    }

    private DataRequest createDataRequest() {
        DefaultAuthenticationMeans authMeans = DefaultAuthenticationMeans.builder().build();
        return new DataRequest(BASE_URL, signer, authMeans, ACCESS_TOKEN, PSU_IP_ADDRESS, false);
    }

    private StetAccountsResponseDTO createStetAccountsResponseDTO(List<StetAccountDTO> accountDTOs) {
        return TestStetAccountsResponseDTO.builder()
                .accounts(accountDTOs)
                .build();
    }

    private List<StetAccountDTO> createStetAccountDTOs() {
        return Arrays.asList(
                createStetAccountDTO("FR6117569000701242388482K67"),
                createStetAccountDTO("FR7814508000402471669349A04"));
    }

    private StetAccountDTO createStetAccountDTO(String iban) {
        return TestStetAccountDTO.builder()
                .iban(iban)
                .other(new HashMap<>())
                .currency(CurrencyCode.EUR)
                .area(new HashMap<>())
                .build();
    }

    private Map<String, Object> createUpdateConsentBody(StetAccountsResponseDTO accountsResponseDTO) {
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> balanceTransactionPayload = accountsResponseDTO.getAccounts().stream()
                .map(account -> {
                    Map<String, Object> consentDetails = new HashMap<>();
                    consentDetails.put("iban", account.getIban());
                    consentDetails.put("other", account.getOther());
                    consentDetails.put("currency", account.getCurrency());
                    if (Objects.nonNull(account.getArea())) {
                        consentDetails.put("area", account.getArea());
                    }
                    return consentDetails;
                })
                .collect(Collectors.toList());
        payload.put("psuIdentity", "true");
        payload.put("trustedBeneficiaries", "false");
        payload.put("balances", balanceTransactionPayload);
        payload.put("transactions", balanceTransactionPayload);
        return payload;
    }
}
