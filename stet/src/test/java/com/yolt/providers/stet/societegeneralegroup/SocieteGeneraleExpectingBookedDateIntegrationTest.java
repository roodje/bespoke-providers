package com.yolt.providers.stet.societegeneralegroup;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.societegeneralegroup.pri.config.SocieteGeneralePriProperties;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.stet.societegeneralegroup.common.auth.SocieteGeneraleAuthenticationMeansSupplier.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = SocieteGeneraleTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/societegenerale/ais/expecting-booking-date", httpsPort = 0, port = 0)
@ActiveProfiles("societegenerale")
public class SocieteGeneraleExpectingBookedDateIntegrationTest {

    //TODO: I'm not able to get ENT/PRO fetch data response from sandbox at the moment, but I will add necessary test
    // when it would become available


    private static final String CLIENT_SECRET = "55ffxxxx7eeaxxxx8dc8xxxx4d61xxxx";
    private static final UUID TRANSPORT_KEY_ID_ROTATION = UUID.randomUUID();
    private static final UUID SIGNING_KEY_ID_ROTATION = UUID.randomUUID();

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String CLIENT_ID = "f330xxxx6fe8xxxx6edbxxxxd15axxxx";
    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = prepareMeans();
    private static final String ACCESS_TOKEN = "THE-ACCESS-TOKEN";
    private static final ZoneId PARIS_ZONE_ID = ZoneId.of("Europe/Paris");

    @Autowired
    @Qualifier("SocieteGeneralePriDataProviderV6")
    private UrlDataProvider priDataProvider;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private SocieteGeneralePriProperties properties;

    private RestTemplateManager restTemplateManager = new SimpleRestTemplateManagerMock();

    @Mock
    private Signer signer;

    @Test
    public void shouldFetchDataSuccessfully() throws JsonProcessingException, TokenInvalidException, ProviderFetchDataException {
        // given
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, createAuthorizedJsonProviderState(objectMapper, properties, ACCESS_TOKEN), new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setTransactionsFetchStartTime(LocalDateTime.parse("2021-07-13T17:17:13Z", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).toInstant(ZoneOffset.of("+01:00")))
                .build();
        when(signer.sign(any(byte[].class), any(UUID.class), any(SignatureAlgorithm.class))).thenReturn("signed string");

        // when
        DataProviderResponse dataProviderResponse = priDataProvider.fetchData(request);

        // then
        ProviderAccountDTO expectedCashAccount = getExpectedCashAccount();
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        accounts.forEach(ProviderAccountDTO::validate);
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0)).isEqualToIgnoringGivenFields(expectedCashAccount, "transactions", "lastRefreshed");

        List<ProviderTransactionDTO> cashAccountTransactions = accounts.get(0).getTransactions();
        assertThat(cashAccountTransactions).hasSize(0);
    }

    private ProviderAccountDTO getExpectedCashAccount() {
        List<BalanceDTO> balanceDTOs = new ArrayList<>();
        balanceDTOs.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_AVAILABLE)
                .balanceAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("344.50"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .lastChangeDateTime(ZonedDateTime.of(LocalDate.of(2021, Month.OCTOBER, 1), LocalTime.MIN, PARIS_ZONE_ID))
                .build());

        ExtendedAccountDTO extendedAccountDTO = ExtendedAccountDTO.builder()
                .resourceId("30003000000045120025007158200050")
                .accountReferences(Collections.singletonList(AccountReferenceDTO.builder()
                        .type(AccountReferenceType.IBAN)
                        .value("FR7630003045120025007158210")
                        .build()))
                .currency(CurrencyCode.EUR)
                .name("Compte Bancaire")
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .bic("SOGEFRPP")
                .usage(UsageType.PRIVATE)
                .balances(balanceDTOs)
                .build();

        ExtendedTransactionDTO extendedTransactionDTO1 = getExtendedTransactionDTO1();
        List<ProviderTransactionDTO> transactionDTOs = getProviderTransactionDTOS(extendedTransactionDTO1);

        ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "FR7630003045120025007158210");
        providerAccountNumberDTO.setHolderName("Compte Bancaire");

        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(Instant.now().atZone(ZoneOffset.UTC))
                .availableBalance(new BigDecimal("344.50"))
                .currentBalance(new BigDecimal("344.50"))
                .accountId("30003000000045120025007158200050")
                .accountNumber(providerAccountNumberDTO)
                .bic("SOGEFRPP")
                .name("Compte Bancaire")
                .currency(CurrencyCode.EUR)
                .transactions(transactionDTOs)
                .extendedAccount(extendedAccountDTO)
                .build();
    }

    private ExtendedTransactionDTO getExtendedTransactionDTO1() {
        return ExtendedTransactionDTO.builder()
                .status(TransactionStatus.PENDING)
                .entryReference("7517458")
                .bookingDate(ZonedDateTime.of(LocalDate.of(2021, Month.OCTOBER, 20), LocalTime.MIN, PARIS_ZONE_ID))
                .valueDate(ZonedDateTime.of(LocalDate.of(2021, Month.OCTOBER, 20), LocalTime.MIN, PARIS_ZONE_ID))
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("-1.00"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .remittanceInformationUnstructured("ABONNEMENT MON COMPTE EN BREF")
                .transactionIdGenerated(false)
                .build();
    }

    private List<ProviderTransactionDTO> getProviderTransactionDTOS(ExtendedTransactionDTO extendedTransactionDTO1) {
        List<ProviderTransactionDTO> transactionDTOs = new ArrayList<>();
        transactionDTOs.add(ProviderTransactionDTO.builder()
                .externalId("7517458")
                .dateTime(ZonedDateTime.of(LocalDate.of(2021, Month.OCTOBER, 20), LocalTime.MIN, PARIS_ZONE_ID))
                .amount(new BigDecimal("1.00"))
                .status(TransactionStatus.PENDING)
                .type(ProviderTransactionType.DEBIT)
                .description("ABONNEMENT MON COMPTE EN BREF")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(extendedTransactionDTO1)
                .build());
        return transactionDTOs;
    }

    private static Map<String, BasicAuthenticationMean> prepareMeans() {
        Map<String, BasicAuthenticationMean> means = new HashMap<>();
        means.put(CLIENT_ID_NAME, new BasicAuthenticationMean(API_KEY.getType(), CLIENT_ID));
        means.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(API_SECRET.getType(), CLIENT_SECRET));
        means.put(CLIENT_TRANSPORT_KEY_ID_ROTATION, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID_ROTATION.toString()));
        means.put(CLIENT_TRANSPORT_CERTIFICATE_ROTATION, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(),
                readCertificate("certificates/societegenerale/yolt_certificate_transport.pem")));
        means.put(CLIENT_SIGNING_KEY_ID_ROTATION, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID_ROTATION.toString()));
        means.put(CLIENT_SIGNING_CERTIFICATE_ROTATION, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(),
                readCertificate("certificates/societegenerale/yolt_certificate_signing.pem")));
        return means;
    }

    private static String readCertificate(String certificatePath) {
        try {
            URI fileURI = SocieteGeneraleExpectingBookedDateIntegrationTest.class
                    .getClassLoader()
                    .getResource(certificatePath)
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SneakyThrows
    private static String createAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties, String accessToken) {
        Region region = properties.getRegions().get(0);
        DataProviderState providerState = DataProviderState.authorizedProviderState(region, accessToken);
        return objectMapper.writeValueAsString(providerState);
    }
}
