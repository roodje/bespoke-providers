package com.yolt.providers.openbanking.ais.hsbcgroup.pis.scheduled;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupSampleAuthenticationMeansV2;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme.SORTCODEACCOUNTNUMBER;
import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains creation of uk payment when bad request HTTP status code is returned from bank
 * <p>
 * Covered flows:
 * - 200 with missing consent id. In that case response body validation should failed
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/hsbcgroup/pis-3.1.6/scheduled/happy-flow/grant-type",
        "classpath:/stubs/hsbcgroup/pis-3.1.6/scheduled/missing-consent-id"}, httpsPort = 0, port = 0)
class HsbcGroupUkDomesticPaymentProviderInitiateMissingConsentIdTest {

    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";
    private static final String VALID_CREDITOR_NUMBER = "1802968485593088";
    private static final String VALID_DEBTOR_NUMBER = "8272908780568576";

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private final AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;
    private Clock clock = Clock.systemUTC();

    @Autowired
    @Qualifier("HsbcPaymentProviderV14")
    private GenericBasePaymentProviderV3 hsbcPaymentProviderV14;

    @Autowired
    @Qualifier("FirstDirectPaymentProviderV14")
    private GenericBasePaymentProviderV3 firstDirectPaymentProviderV14;

    private Stream<UkDomesticPaymentProvider> getProviders() {
        return Stream.of(hsbcPaymentProviderV14,
                firstDirectPaymentProviderV14);
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d10f24f4-032a-4843-bfc9-22b599c7ae2d");
        authenticationMeans = new HsbcGroupSampleAuthenticationMeansV2().getHsbcGroupSampleAuthenticationMeansForPis();
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnResultWithStatusInitiationErrorInPecMetadataForCreatePaymentWhenConsentIdIsNotProvidedInBankResponse(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        // given
        InitiateUkDomesticScheduledPaymentRequest request = new InitiateUkDomesticScheduledPaymentRequest(
                createInitiateUkDomesticScheduledPaymentRequestDTO(),
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference);

        // when
        InitiateUkDomesticPaymentResponseDTO result = paymentProvider.initiateScheduledPayment(request);

        // then
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            {"Data":{"Permission":"Create","Status":"AwaitingAuthorisation","StatusUpdateDateTime":"2020-06-16T09:50:51+00:00","CreationDateTime":"2020-06-16T09:50:51+00:00","Initiation":{"InstructionIdentification":"20200515101750462-522347ee-5e0","RequestedExecutionDateTime":"2022-02-01T00:00:00+00:00","EndToEndIdentification":"35B64F93","InstructedAmount":{"Amount":"0.01","Currency":"GBP"},"DebtorAccount":{"SchemeName":"UK.OBIE.SortCodeAccountNumber","Identification":"8272908780568576","Name":"Alex Mitchell"},"CreditorAccount":{"SchemeName":"UK.OBIE.SortCodeAccountNumber","Identification":"1802968485593088","Name":"Jordan Bell"},"RemittanceInformation":{"Unstructured":"Remittance Unstructured","Reference":"REF0123456789-0123"}}},"Links":{"Self":"/v3.1/pisp/domestic-scheduled-payment-consents/462d1a04-4g74-42d7-ba69-0bbe1e5fef68"},"Meta":{"TotalPages":1},"Risk":{"PaymentContextCode":"PartyToParty"}}""");
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                });
    }

    private InitiateUkDomesticScheduledPaymentRequestDTO createInitiateUkDomesticScheduledPaymentRequestDTO() {
        return new InitiateUkDomesticScheduledPaymentRequestDTO(
                "35B64F93",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                new UkAccountDTO(VALID_CREDITOR_NUMBER, SORTCODEACCOUNTNUMBER, "Jordan Bell", null),
                new UkAccountDTO(VALID_DEBTOR_NUMBER, SORTCODEACCOUNTNUMBER, "Alex Mitchell", null),
                "Remittance Unstructured",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "REF0123456789-0123"),
                OffsetDateTime.now(clock).plusDays(10).truncatedTo(ChronoUnit.SECONDS)
        );
    }

}