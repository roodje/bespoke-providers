package com.yolt.providers.openbanking.ais.bankofirelandgroup.pis;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandGroupApp;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandSampleTypedAuthMeans;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains creation of uk payment when bad request HTTP status code is returned from bank
 * <p>
 * Covered flows:
 * - 400 Bad Request upon creation of payment
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BankOfIrelandGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bankofireland")
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/bankofireland/pis-3.0.0/single/bad-request",
        "classpath:/stubs/bankofireland/pis-3.0.0/single/happy-flow/grant-type"}, httpsPort = 0, port = 0)
class BankOfIrelandUkDomesticPaymentProviderInitiate400IntegrationTest {

    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final String INVALID_CREDITOR_NUMBER = "98765432109876";
    private final AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;

    @Autowired
    @Qualifier("BankOfIrelandPaymentProviderV1")
    private GenericBasePaymentProviderV3 bankOfIrelandPaymentProviderV1;


    private Stream<UkDomesticPaymentProvider> getProviders() {
        return Stream.of(bankOfIrelandPaymentProviderV1);
    }

    @BeforeEach
    void beforeEach() {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d10f24f4-032a-4843-bfc9-22b599c7ae2d");
        authenticationMeans = BankOfIrelandSampleTypedAuthMeans.getSampleAuthMeans();
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnResultWithStatusInitiationErrorInPecMetadataForCreatePaymentWhenBadRequestHttpCodeIsReceivedFromBank(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        // given
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                createInitiateUkDomesticPaymentRequestDTO(INVALID_CREDITOR_NUMBER),
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference);

        // when
        InitiateUkDomesticPaymentResponseDTO result = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("400 BadRequest");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            [{"ErrorCode":"UK.OBIE.Field.Missing","Message":"End to end identification is missing","Path":"Data.Initiation.InstructionIdentification","Url":"<url to the api reference for Payment Inititaion API>"},{"ErrorCode":"UK.OBIE.Unsupported.Scheme","Message":"Scheme name supplied is not supported","Path":"Data.Initiation.CreditorAccount.SchemeName","Url":"<url to the online documentation referring supported scheme names>"}]""");
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                });
    }

    private InitiateUkDomesticPaymentRequestDTO createInitiateUkDomesticPaymentRequestDTO(String identification) {
        UkAccountDTO creditorAccount = new UkAccountDTO(identification, AccountIdentifierScheme.IBAN, "P. Jantje", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "END1234",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                creditorAccount,
                null,
                "Unstructured",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "MalformedPayment")
        );
    }

}