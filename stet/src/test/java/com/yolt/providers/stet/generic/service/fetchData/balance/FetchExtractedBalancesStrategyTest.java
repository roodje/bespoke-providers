package com.yolt.providers.stet.generic.service.fetchData.balance;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.TestStetAccountDTO;
import com.yolt.providers.stet.generic.dto.TestStetBalanceDTO;
import com.yolt.providers.stet.generic.dto.TestStetBalancesResponseDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.dto.balance.StetBalancesResponseDTO;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchExtractedBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FetchExtractedBalancesStrategyTest {

    private static final String ACCESS_TOKEN = "5b1a0a0f-2774-478d-9796-72e1582c0a6a";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Mock
    private HttpClient httpClient;

    @Mock
    private DefaultAuthenticationMeans authMeans;

    @Mock
    private FetchDataRestClient restClient;

    @Mock
    private Signer signer;

    private FetchExtractedBalancesStrategy fetchBalancesStrategy;

    @BeforeEach
    void initialize() {
        fetchBalancesStrategy = new FetchExtractedBalancesStrategy(restClient);
    }

    @Test
    void shouldReturnBalancesWhichWereExtractedFromAccount() throws TokenInvalidException {
        // given
        StetAccountDTO accountDTO = createStetAccountDTO(Collections.singletonList(createStetBalanceDTO()));
        DataRequest dataRequest = createDataRequest();

        // when
        List<StetBalanceDTO> stetBalanceDTOs = fetchBalancesStrategy
                .fetchBalances(httpClient, "/accounts/1/balances", dataRequest, accountDTO);

        // then
        assertThat(stetBalanceDTOs).isSameAs(accountDTO.getBalances());
    }

    @Test
    void shouldReturnBalancesAfterFetchingThemWhenAccountDoNotContainBalances() throws TokenInvalidException {
        // given
        StetAccountDTO accountDTO = createStetAccountDTO(Collections.emptyList());
        StetBalancesResponseDTO expectedBalancesResponseDTO = createStetBalanceResponseDTO();
        DataRequest dataRequest = createDataRequest();

        when(restClient.getBalances(any(HttpClient.class), anyString(), any(DataRequest.class)))
                .thenReturn(expectedBalancesResponseDTO);

        // when
        List<StetBalanceDTO> stetBalanceDTOs = fetchBalancesStrategy
                .fetchBalances(httpClient, "/accounts/1/balances", dataRequest, accountDTO);

        // then
        assertThat(stetBalanceDTOs).containsExactlyElementsOf(expectedBalancesResponseDTO.getBalances());

        verify(restClient)
                .getBalances(httpClient, "/accounts/1/balances", dataRequest);
    }

    private DataRequest createDataRequest() {
        return new DataRequest("http://localhost", signer, authMeans, ACCESS_TOKEN, PSU_IP_ADDRESS, false);
    }

    private StetAccountDTO createStetAccountDTO(List<StetBalanceDTO> balanceDTOs) {
        return TestStetAccountDTO.builder()
                .resourceId("1")
                .balances(balanceDTOs)
                .build();
    }

    private StetBalancesResponseDTO createStetBalanceResponseDTO() {
        return TestStetBalancesResponseDTO.builder()
                .balances(Collections.singletonList(createStetBalanceDTO()))
                .build();
    }

    private StetBalanceDTO createStetBalanceDTO() {
        return TestStetBalanceDTO.builder()
                .type(StetBalanceType.XPCD)
                .build();
    }
}
