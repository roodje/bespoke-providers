package com.yolt.providers.openbanking.ais.hsbcgroup.pis.single;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
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
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/hsbcgroup/pis-3.1.6/single/bad-request",
        "classpath:/stubs/hsbcgroup/pis-3.1.6/single/happy-flow/grant-type"}, httpsPort = 0, port = 0)
class HsbcGroupUkDomesticPaymentProviderInitiate400IntegrationTest {

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
    @Qualifier("HsbcPaymentProviderV13")
    private GenericBasePaymentProviderV2 hsbcPaymentProviderV13;

    @Autowired
    @Qualifier("HsbcPaymentProviderV14")
    private GenericBasePaymentProviderV3 hsbcPaymentProviderV14;

    @Autowired
    @Qualifier("FirstDirectPaymentProviderV13")
    private GenericBasePaymentProviderV2 firstDirectPaymentProviderV13;

    @Autowired
    @Qualifier("FirstDirectPaymentProviderV14")
    private GenericBasePaymentProviderV3 firstDirectPaymentProviderV14;

    private Stream<UkDomesticPaymentProvider> getProviders() {
        return Stream.of(hsbcPaymentProviderV13, firstDirectPaymentProviderV13,
                hsbcPaymentProviderV14, firstDirectPaymentProviderV14);
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d10f24f4-032a-4843-bfc9-22b599c7ae2d");
        authenticationMeans = new HsbcGroupSampleAuthenticationMeansV2().getHsbcGroupSampleAuthenticationMeansForPis();
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
                "",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                creditorAccount,
                null,
                "Unstructured",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "MalformedPayment")
        );
    }

}