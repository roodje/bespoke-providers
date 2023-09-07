package com.yolt.providers.stet.societegeneralegroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.societegeneralegroup.common.dto.AccessTokenResponseDTO;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.stet.societegeneralegroup.common.auth.SocieteGeneraleAuthenticationMeansSupplier.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = SocieteGeneraleTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/societegenerale/ais/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("societegenerale")
public class SocieteGeneraleDataProviderIntegrationTest {

    //TODO: I'm not able to get ENT/PRO fetch data response from sandbox at the moment, but I will add necessary test
    // when it would become available

    private static final String CLIENT_SECRET = "55ffxxxx7eeaxxxx8dc8xxxx4d61xxxx";
    private static final UUID TRANSPORT_KEY_ID_ROTATION = UUID.randomUUID();
    private static final UUID SIGNING_KEY_ID_ROTATION = UUID.randomUUID();

    private static final String REDIRECT_URL = "https://redirect.url";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String CLIENT_ID = "f330xxxx6fe8xxxx6edbxxxxd15axxxx";
    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = prepareMeans();
    private static final String ACCESS_TOKEN = "THE-ACCESS-TOKEN";
    private static final UUID CODE = UUID.fromString("71417ee9-2534-451f-b4bb-5d487ac7ea5a");
    private static final ZoneId PARIS_ZONE_ID = ZoneId.of("Europe/Paris");

    @Autowired
    @Qualifier("SocieteGeneralePriDataProviderV6")
    private UrlDataProvider priDataProvider;

    @Autowired
    @Qualifier("SocieteGeneraleProDataProviderV5")
    private UrlDataProvider proDataProvider;

    @Autowired
    @Qualifier("SocieteGeneraleEntDataProviderV5")
    private UrlDataProvider entDataProvider;

    @Autowired
    @Qualifier("CreditDuNordDataProviderV1")
    private UrlDataProvider creditDuNordDataProvider;

    @Autowired
    @Qualifier("BanqueCourtoisDataProviderV1")
    private UrlDataProvider banqueCourtoisDataProvider;

    @Autowired
    @Qualifier("BanqueKolbDataProviderV1")
    private UrlDataProvider banqueKolbDataProvider;

    @Autowired
    @Qualifier("BanqueLaydernierDataProviderV1")
    private UrlDataProvider banqueLaydernierDataProvider;

    @Autowired
    @Qualifier("BanqueNugerDataProviderV1")
    private UrlDataProvider banqueNugerDataProvider;

    @Autowired
    @Qualifier("BanqueRhoneAlpesDataProviderV1")
    private UrlDataProvider banqueRhoneAlpesDataProvider;

    @Autowired
    @Qualifier("BanqueTarneaudDataProviderV1")
    private UrlDataProvider banqueTarneaudDataProvider;

    @Autowired
    @Qualifier("SocieteDeBanqueMonacoDataProviderV1")
    private UrlDataProvider societeDeBanqueMonacoDataProvider;

    @Autowired
    @Qualifier("SocieteMarseillaiseDeCreditDataProviderV1")
    private UrlDataProvider societeMarseillaiseDeCreditDataProvider;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private SocieteGeneralePriProperties properties;

    private RestTemplateManager restTemplateManager = new SimpleRestTemplateManagerMock();

    @Mock
    private Signer signer;

    @ParameterizedTest
    @MethodSource("getAllSocieteGeneraleDataProviders")
    public void shouldReturnProperMapOfAuthenticationMeans(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthenticationMeans).containsOnlyKeys(CLIENT_ID_NAME, CLIENT_SECRET_NAME, CLIENT_SIGNING_KEY_ID_ROTATION,
                CLIENT_SIGNING_CERTIFICATE_ROTATION, CLIENT_TRANSPORT_KEY_ID_ROTATION, CLIENT_TRANSPORT_CERTIFICATE_ROTATION);
    }

    @ParameterizedTest
    @MethodSource("getAllSocieteGeneraleDataProviders")
    public void shouldReturnCorrectLoginUrl(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState("state")
                .setAuthenticationMeans(AUTH_MEANS)
                .build();

        // when
        RedirectStep step = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        assertThat(step.getExternalConsentId()).isNull();
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(step.getRedirectUrl()).build();
        assertThat(uriComponents.getPath()).isEqualTo("/oauth2/authorize");
        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        assertThat(queryParams.getFirst("client_id")).isEqualTo(CLIENT_ID);
        assertThat(queryParams.getFirst("response_type")).isEqualTo("code");
        assertThat(queryParams.getFirst("scope")).isEqualTo("aisp");
        assertThat(queryParams.getFirst("redirect_uri")).isEqualTo(REDIRECT_URL);
        assertThat(queryParams.getFirst("state")).isEqualTo("state");
    }

    @ParameterizedTest
    @MethodSource("getAllSocieteGeneraleDataProviders")
    public void shouldCreateNewAccessMeansSuccessfully(UrlDataProvider dataProvider) throws IOException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + CODE)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(createPreAuthorizedJsonProviderState(objectMapper, properties))
                .build();

        // when
        AccessMeansOrStepDTO accessMeans = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeans.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        DataProviderState dataProviderState = objectMapper.readValue(accessMeans.getAccessMeans().getAccessMeans(), DataProviderState.class);
        assertThat(dataProviderState.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(dataProviderState.getRefreshToken()).isEqualTo("2c3d535c-af8f-407e-8af1-178396a9b858");
        assertThat(dataProviderState.isRefreshed()).isEqualTo(false);
    }

    @ParameterizedTest
    @MethodSource("getAllSocieteGeneraleDataProviders")
    public void shouldRefreshAccessMeansSuccessfully(UrlDataProvider dataProvider) throws IOException, TokenInvalidException {
        // given
        String accessMeans = "{\n" +
                             "      \"access_token\": \"THE-ACCESS-TOKEN\",\n" +
                             "      \"token_type\": \"Bearer\",\n" +
                             "      \"expires_in\": 900,\n" +
                             "      \"refresh_token\": \"2c3d535c-af8f-407e-8af1-178396a9b858\"\n" +
                             "    }";
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, accessMeans, new Date(), new Date());
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO refreshedAccessMeans = dataProvider.refreshAccessMeans(request);
        DataProviderState refreshedToken = objectMapper.readValue(refreshedAccessMeans.getAccessMeans(), DataProviderState.class);

        // then
        assertThat(refreshedAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(refreshedToken.getAccessToken()).isEqualTo("THE-REFRESHED-ACCESS-TOKEN");
        assertThat(refreshedToken.getRefreshToken()).isEqualTo("4f53e19c-2e0e-418a-b57c-7eb94cc76ee7");
        assertThat(refreshedToken.isRefreshed()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getAllSocieteGeneraleDataProviders")
    public void shouldFetchDataSuccessfully(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
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
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(request);

        // then
        ProviderAccountDTO expectedCashAccount = getExpectedCashAccount();
        ProviderAccountDTO expectedCardAccount = getExpectedCardAccount();
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        accounts.forEach(ProviderAccountDTO::validate);
        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0)).isEqualToIgnoringGivenFields(expectedCashAccount, "transactions", "lastRefreshed");
        assertThat(accounts.get(1)).isEqualToIgnoringGivenFields(expectedCardAccount, "transactions", "lastRefreshed");

        List<ProviderTransactionDTO> cashAccountTransactions = accounts.get(0).getTransactions();
        assertThat(cashAccountTransactions).hasSize(1)
                .contains(expectedCashAccount.getTransactions().get(0));

        List<ProviderTransactionDTO> cardAccountTransactions = accounts.get(1).getTransactions();
        assertThat(cardAccountTransactions)
                .hasSize(1)
                .contains(expectedCardAccount.getTransactions().get(0));
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

        ExtendedTransactionDTO extendedTransactionDTO2 = getExtendedTransactionDTO2();

        List<ProviderTransactionDTO> transactionDTOs = getProviderTransactionDTOS(extendedTransactionDTO1, extendedTransactionDTO2);

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
                .bookingDate(ZonedDateTime.of(LocalDate.of(2019, Month.OCTOBER, 4), LocalTime.MIN, PARIS_ZONE_ID))
                .valueDate(ZonedDateTime.of(LocalDate.of(2019, Month.OCTOBER, 4), LocalTime.MIN, PARIS_ZONE_ID))
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("-1.00"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .remittanceInformationUnstructured("ABONNEMENT MON COMPTE EN BREF")
                .transactionIdGenerated(false)
                .build();
    }

    private ExtendedTransactionDTO getExtendedTransactionDTO2() {
        return ExtendedTransactionDTO.builder()
                .status(TransactionStatus.BOOKED)
                .entryReference("7442971")
                .bookingDate(ZonedDateTime.of(LocalDate.of(2019, Month.OCTOBER, 4), LocalTime.MIN, PARIS_ZONE_ID))
                .valueDate(ZonedDateTime.of(LocalDate.of(2019, Month.OCTOBER, 4), LocalTime.MIN, PARIS_ZONE_ID))
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("11.00"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .remittanceInformationUnstructured("VIREMENT RECU DE: COUPLE VUDO REF: 1000000410244")
                .transactionIdGenerated(false)
                .build();
    }

    private List<ProviderTransactionDTO> getProviderTransactionDTOS(ExtendedTransactionDTO extendedTransactionDTO1, ExtendedTransactionDTO extendedTransactionDTO2) {
        List<ProviderTransactionDTO> transactionDTOs = new ArrayList<>();


        transactionDTOs.add(ProviderTransactionDTO.builder()
                .externalId("7442971")
                .dateTime(ZonedDateTime.of(LocalDate.of(2019, Month.OCTOBER, 4), LocalTime.MIN, PARIS_ZONE_ID))
                .amount(new BigDecimal("11.00"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.CREDIT)
                .description("VIREMENT RECU DE: COUPLE VUDO REF: 1000000410244")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(extendedTransactionDTO2)
                .build());
        return transactionDTOs;
    }

    private ProviderAccountDTO getExpectedCardAccount() {
        List<BalanceDTO> balanceDTOs = new ArrayList<>();
        balanceDTOs.add(BalanceDTO.builder()
                .balanceType(BalanceType.NON_INVOICED)
                .balanceAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("314.15"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .lastChangeDateTime(ZonedDateTime.of(LocalDate.of(2021, Month.OCTOBER, 1), LocalTime.MIN, PARIS_ZONE_ID))
                .build());

        balanceDTOs.add(BalanceDTO.builder()
                .balanceType(BalanceType.NON_INVOICED)
                .balanceAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("999.99"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .lastChangeDateTime(ZonedDateTime.of(LocalDate.of(2021, Month.SEPTEMBER, 1), LocalTime.MIN, PARIS_ZONE_ID))
                .build());

        balanceDTOs.add(BalanceDTO.builder()
                .balanceType(BalanceType.CLOSING_BOOKED)
                .balanceAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("200.23"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .lastChangeDateTime(ZonedDateTime.of(LocalDate.of(2021, Month.OCTOBER, 1), LocalTime.MIN, PARIS_ZONE_ID))
                .build());

        ExtendedAccountDTO extendedAccountDTO = ExtendedAccountDTO.builder()
                .resourceId("30003000000045120025007158200050-CARD")
                .accountReferences(Collections.singletonList(AccountReferenceDTO.builder()
                        .type(AccountReferenceType.IBAN)
                        .value("FR7630003045120025007158210")
                        .build()))
                .currency(CurrencyCode.EUR)
                .name("Compte Bancaire")
                .cashAccountType(ExternalCashAccountType.OTHER)
                .bic("SOGEFRPP")
                .usage(UsageType.PRIVATE)
                .balances(balanceDTOs)
                .build();

        ExtendedTransactionDTO extendedTransactionDTO1 = getExtendedCardAccountTransactionDTO1();

        ExtendedTransactionDTO extendedTransactionDTO2 = getExtendedCardAccountTransactionDTO2();

        List<ProviderTransactionDTO> transactionDTOs = getProviderCardAccountTransactionDTOS(extendedTransactionDTO1, extendedTransactionDTO2);

        ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "FR7630003045120025007158210");
        providerAccountNumberDTO.setHolderName("Compte Bancaire");

        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CREDIT_CARD)
                .lastRefreshed(Instant.now().atZone(ZoneOffset.UTC))
                .availableBalance(new BigDecimal("-200.23"))
                .currentBalance(new BigDecimal("-200.23"))
                .accountId("30003000000045120025007158200050-CARD")
                .accountNumber(providerAccountNumberDTO)
                .bic("SOGEFRPP")
                .name("Compte Bancaire")
                .currency(CurrencyCode.EUR)
                .transactions(transactionDTOs)
                .creditCardData(ProviderCreditCardDTO.builder().availableCreditAmount(new BigDecimal("-200.23")).build())
                .extendedAccount(extendedAccountDTO)
                .build();
    }

    private ExtendedTransactionDTO getExtendedCardAccountTransactionDTO1() {
        return ExtendedTransactionDTO.builder()
                .status(TransactionStatus.BOOKED)
                .entryReference("1234124")
                .bookingDate(ZonedDateTime.of(LocalDate.of(2020, Month.DECEMBER, 17), LocalTime.MIN, PARIS_ZONE_ID))
                .valueDate(ZonedDateTime.of(LocalDate.of(2020, Month.DECEMBER, 17), LocalTime.MIN, PARIS_ZONE_ID))
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("-2.00"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .remittanceInformationUnstructured("ABONNEMENT MON COMPTE EN BREF")
                .transactionIdGenerated(false)
                .build();
    }

    private ExtendedTransactionDTO getExtendedCardAccountTransactionDTO2() {
        return ExtendedTransactionDTO.builder()
                .status(TransactionStatus.PENDING)
                .entryReference("1234125")
                .bookingDate(ZonedDateTime.of(LocalDate.of(2020, Month.DECEMBER, 17), LocalTime.MIN, PARIS_ZONE_ID))
                .valueDate(ZonedDateTime.of(LocalDate.of(2020, Month.DECEMBER, 17), LocalTime.MIN, PARIS_ZONE_ID))
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("-3.00"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .remittanceInformationUnstructured("ABONNEMENT MON COMPTE EN BREF")
                .transactionIdGenerated(false)
                .build();
    }

    private List<ProviderTransactionDTO> getProviderCardAccountTransactionDTOS(ExtendedTransactionDTO extendedTransactionDTO1, ExtendedTransactionDTO extendedTransactionDTO2) {
        List<ProviderTransactionDTO> transactionDTOs = new ArrayList<>();
        transactionDTOs.add(ProviderTransactionDTO.builder()
                .externalId("1234124")
                .dateTime(ZonedDateTime.of(LocalDate.of(2020, Month.DECEMBER, 17), LocalTime.MIN, PARIS_ZONE_ID))
                .amount(new BigDecimal("2.00"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.DEBIT)
                .description("ABONNEMENT MON COMPTE EN BREF")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(extendedTransactionDTO1)
                .build());

        transactionDTOs.add(ProviderTransactionDTO.builder()
                .externalId("1234125")
                .dateTime(ZonedDateTime.of(LocalDate.of(2020, Month.DECEMBER, 17), LocalTime.MIN, PARIS_ZONE_ID))
                .amount(new BigDecimal("3.00"))
                .status(TransactionStatus.PENDING)
                .type(ProviderTransactionType.DEBIT)
                .description("ABONNEMENT MON COMPTE EN BREF")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(extendedTransactionDTO2)
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
            URI fileURI = SocieteGeneraleDataProviderIntegrationTest.class
                    .getClassLoader()
                    .getResource(certificatePath)
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Stream<UrlDataProvider> getAllSocieteGeneraleDataProviders() {
        return Stream.of(priDataProvider, entDataProvider, proDataProvider, creditDuNordDataProvider,
                banqueCourtoisDataProvider, banqueKolbDataProvider, banqueLaydernierDataProvider,
                banqueNugerDataProvider, banqueRhoneAlpesDataProvider, banqueTarneaudDataProvider,
                societeDeBanqueMonacoDataProvider, societeMarseillaiseDeCreditDataProvider);
    }

    @Test
    public void shouldThrowTokenInvalidExceptionDuringFetchData() throws JsonProcessingException {
        // given
        AccessTokenResponseDTO accessTokenResponseDTO = new AccessTokenResponseDTO();
        accessTokenResponseDTO.setAccessToken("THE-ACCESS-TOKEN-EXPIRED");
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, objectMapper.writeValueAsString(accessTokenResponseDTO), new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        assertThatThrownBy(() -> priDataProvider.fetchData(request)).isInstanceOf(TokenInvalidException.class);
    }

    @SneakyThrows
    private String createPreAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties) {
        Region region = properties.getRegions().get(0);
        AuthorizationRedirect authRedirect = AuthorizationRedirect.create(region.getAuthUrl());
        DataProviderState providerState = DataProviderState.preAuthorizedProviderState(region, authRedirect);
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    private static String createAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties, String accessToken) {
        Region region = properties.getRegions().get(0);
        DataProviderState providerState = DataProviderState.authorizedProviderState(region, accessToken);
        return objectMapper.writeValueAsString(providerState);
    }
}
