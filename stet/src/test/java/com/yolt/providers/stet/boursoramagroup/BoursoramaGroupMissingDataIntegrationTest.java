package com.yolt.providers.stet.boursoramagroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.stet.boursoramagroup.boursorama.BoursoramaDataProviderV4;
import com.yolt.providers.stet.boursoramagroup.boursorama.config.BoursoramaProperties;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BoursoramaGroupTestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("boursorama")
@AutoConfigureWireMock(stubs = "classpath:/stubs/boursorama/ais/missing-data/", httpsPort = 0, port = 0)
public class BoursoramaGroupMissingDataIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImtpZCI6IkhTMjU2In0.eyJqdGkiOiJjZTdiNTVmNzlmY2ZlIiwic3ViIjoiZHNwMiIsImF1ZCI6Ii4qXFwuYm91cnNvcmFtYVxcLmNvbSIsImV4cCI6MTg2NDIyNDY1NCwiaWF0IjoxNTQ4ODY0NjU0LCJuYmYiOjE1NDg4NjQ2NTQsInNlc3Npb24iOnsidXNlcklkIjoiMDAwMDAwMDAiLCJsZXZlbCI6IkNVU1RPTUVSIn0sImlzcyI6IkFkbWluIEpXVCBCb3Vyc29yYW1hIiwidXNlckhhc2giOiI3MDM1MmY0MTA2MWVkYTQiLCJvcmciOiJCMTkiLCJvYXV0aCI6ImM2OTdjOWUxZTUxZjg4Y2U2NWJjOGM4NWNmMjhkMDcyYWNmMDQyNTQifQ.3sewgdSK4OJfcsrVK2eqa8FF2jvDfdpiyBuIOh0CMRI";

    private final Signer signer = mock(Signer.class);
    private final Map<String, BasicAuthenticationMean> authenticationMeans = BoursoramaGroupSampleMeans.getAuthMeans();

    @Autowired
    @Qualifier("BoursoramaDataProviderV4")
    BoursoramaDataProviderV4 boursoramaDataProviderV4;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    BoursoramaProperties boursoramaProperties;

    @Autowired
    private RestTemplateManager restTemplateManager;


    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(boursoramaDataProviderV4);
    }

    @BeforeEach
    public void setUp() {
        when(signer.sign(ArgumentMatchers.any(byte[].class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(Base64.toBase64String("TEST-ENCODED-SIGNATURE".getBytes()));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldSuccessfullyFetchDataWhenSomeDataIsMissingInBanksResponse(UrlDataProvider provider) throws ProviderFetchDataException, TokenInvalidException {
        // given
        Instant transactionsFetchStartTime = Instant.parse("2019-06-27T12:23:25Z");

        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAccessMeans(USER_ID,
                        BoursoramaGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, boursoramaProperties, ACCESS_TOKEN),
                        new Date(),
                        new Date())
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(transactionsFetchStartTime)
                .setPsuIpAddress("147.206.96.254")
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(request);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        ProviderAccountDTO account = dataProviderResponse.getAccounts().get(0);
        assertThat(account.getCurrentBalance()).isNull();
        assertThat(account.getAvailableBalance()).isNull();
        assertThat(account.getCurrency()).isNull();
        assertThat(account.getExtendedAccount().getUsage()).isNull();
        assertThat(account.getExtendedAccount().getBalances()).hasSize(1);
        assertThat(account.getExtendedAccount().getBalances().get(0).getBalanceAmount().getAmount()).isEqualTo("1642.68");
        assertThat(account.getExtendedAccount().getBalances().get(0).getBalanceAmount().getCurrency()).isNull();
        assertThat(account.getTransactions()).hasSize(2);
        ProviderTransactionDTO transaction0 = getTransactionWithSpecificOrNullAmount(account.getTransactions(), new BigDecimal("12.00"));
        assertThat(transaction0.getAmount()).isEqualTo("12.00");
        assertThat(transaction0.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction0.getType()).isNull();
        assertThat(transaction0.getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("12.00");
        assertThat(transaction0.getExtendedTransaction().getTransactionAmount().getCurrency()).isNull();
        ProviderTransactionDTO transaction2 = getTransactionWithSpecificOrNullAmount(account.getTransactions(), new BigDecimal("14.00"));
        assertThat(transaction2.getAmount()).isEqualTo("14.00");
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction2.getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-14.00");
        assertThat(transaction2.getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
    }

    private ProviderTransactionDTO getTransactionWithSpecificOrNullAmount(List<ProviderTransactionDTO> transactionsList, BigDecimal amount) {
        if (ObjectUtils.isEmpty(amount)) {
            return transactionsList.stream()
                    .filter(transaction -> ObjectUtils.isEmpty(transaction.getAmount()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("There is no element in list with null amount"));
        }
        return transactionsList.stream()
                .filter(transaction -> amount.equals(transaction.getAmount()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("There is no element in list with amount %d", amount)));
    }
}
