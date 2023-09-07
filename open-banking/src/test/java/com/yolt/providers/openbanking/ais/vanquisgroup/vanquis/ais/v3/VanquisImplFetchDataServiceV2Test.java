package com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.ais.v3;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisGroupSampleTypedAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.auth.VanquisGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.service.ais.fetchdataservice.VanquisGroupFetchDataServiceV3;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VanquisImplFetchDataServiceV2Test {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String OB_3_X_X_SCHEME_PREFIX = "UK.OBIE.";
    private static final Clock clock = Clock.systemUTC();
    private static DefaultAuthMeans authenticationMeans;
    @Captor
    private ArgumentCaptor<String> requestUriCaptor;

    @Mock
    private HttpClient httpClient;
    private VanquisGroupFetchDataServiceV3 fetchDataService;
    private AccessMeans testToken;
    private Instant fromFetchDateMaxTime;
    private Instant fromFetchDateBefore180days;
    private Instant fromFetchDateAfter180days;
    @Mock
    private RestClient restClient;
    @Mock
    private DefaultProperties properties;
    @Mock
    private Function<OBTransaction6, ProviderTransactionDTO> transactionMapper;
    @Mock
    private Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper;
    @Mock
    private Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private UnaryOperator<List<OBAccount6>> accountFiler;
    private Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier = () -> Set.of(OBExternalAccountSubType1Code.CURRENTACCOUNT);

    @BeforeAll
    public static void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = VanquisGroupAuthMeansBuilderV2.createAuthenticationMeans(
                new VanquisGroupSampleTypedAuthenticationMeansV2().getAuthenticationMeans(), "VANQUIS_BANK");
    }

    @BeforeEach
    public void setup() {
        Instant now = Instant.now(clock);
        fromFetchDateMaxTime = now.minus(Period.ofDays(180));
        fromFetchDateAfter180days = now.minus(Period.ofDays(170));
        fromFetchDateBefore180days = now.minus(Period.ofDays(190));

        fetchDataService = Mockito.spy(new VanquisGroupFetchDataServiceV3(
                restClient,
                properties,
                transactionMapper,
                directDebitMapper,
                standingOrderMapper,
                accountMapper,
                accountFiler,
                supportedAccountSupplier,
                DefaultConsentWindow.DURATION,
                "ENDPOINT_VERSION",
                clock
        ));
        testToken = new AccessMeans(now, USER_ID, "accessToken", "refreshToken", new Date(), new Date(), "http://yolt.com/identifier");
    }

    @Test
    public void testGetAccountsAndTransactions_shouldPassFetchDateTimeAsItIsWhenGivenFetchDateAfter180days() throws Exception {
        when(restClient.fetchAccounts(any(HttpClient.class), anyString(), any(AccessMeans.class), anyString(), any(Class.class))).thenReturn(accountResponse());
        when(accountFiler.apply(any())).thenReturn(mockAccountGetResponse());
        when(restClient.fetchTransactions(any(HttpClient.class), requestUriCaptor.capture(), any(AccessMeans.class), anyString(), any(Class.class))).thenReturn(transactionResponse());

        DataProviderResponse accountsAndTransactions = fetchDataService.getAccountsAndTransactions(httpClient,
                authenticationMeans, fromFetchDateAfter180days, testToken);

        String transactionUri = getTransactionUriFromRequestCapture(requestUriCaptor);
        Instant uriBookingDateTime = getDateFromUriQuery(transactionUri);

        assertThat(uriBookingDateTime.isAfter(fromFetchDateMaxTime)).isTrue();
    }

    @Test
    public void testGetAccountsAndTransactions_shouldTrimFetchDateTimeTo6MonthGivenFetchDateBefore180days() throws Exception {
        when(restClient.fetchAccounts(any(HttpClient.class), anyString(), any(AccessMeans.class), anyString(), any(Class.class))).thenReturn(accountResponse());
        when(accountFiler.apply(any())).thenReturn(mockAccountGetResponse());
        when(restClient.fetchTransactions(any(HttpClient.class), requestUriCaptor.capture(), any(AccessMeans.class), anyString(), any(Class.class))).thenReturn(transactionResponse());

        DataProviderResponse accountsAndTransactions = fetchDataService.getAccountsAndTransactions(httpClient,
                authenticationMeans, fromFetchDateBefore180days, testToken);

        String transactionUri = getTransactionUriFromRequestCapture(requestUriCaptor);
        Instant uriBookingDateTime = getDateFromUriQuery(transactionUri);

        assertThat(uriBookingDateTime.isAfter(fromFetchDateMaxTime)).isTrue();
    }

    private OBReadTransaction6 transactionResponse() {
        OBReadTransaction6 response = new OBReadTransaction6();
        response.setData(new OBReadDataTransaction6());
        return response;
    }

    private OBReadAccount6 accountResponse() {
        OBReadAccount6 response = new OBReadAccount6();
        response.links(new Links());
        response.setData(new OBReadAccount6Data());
        return response;
    }

    private String getTransactionUriFromRequestCapture(ArgumentCaptor<String> requestUriCaptor) {
        return requestUriCaptor.getAllValues()
                .stream()
                .filter(line -> line.contains("fromBookingDateTime"))
                .findAny()
                .get();
    }

    private Instant getDateFromUriQuery(String uriQeury) {
        return Instant.parse(
                uriQeury.substring(uriQeury.indexOf("=") + 1, uriQeury.length()) + "Z");
    }

    private List<OBAccount6> mockAccountGetResponse() {
        List<OBAccount6> accountGETResponse1List = new ArrayList<>();
        accountGETResponse1List.add(
                new OBAccount6()
                        .accountId("1")
                        .account(createAccount())
                        .currency("EUR")
                        .accountSubType(OBExternalAccountSubType1Code.CURRENTACCOUNT)
                        .nickname("nickname")
                        .servicer(new OBBranchAndFinancialInstitutionIdentification50().schemeName(OB_3_X_X_SCHEME_PREFIX + "BICFI").identification("servicer id"))
        );
        return accountGETResponse1List;
    }

    private List<OBAccount4Account> createAccount() {
        return new ArrayList<>() {{
            add(new OBAccount4Account()
                    .identification("identification")
                    .schemeName(OB_3_X_X_SCHEME_PREFIX + "IBAN")
                    .secondaryIdentification("secondary identification"));
        }};
    }
}