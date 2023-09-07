package com.yolt.providers.openbanking.ais.hsbcgroup.pis.single;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupSampleAuthenticationMeansV2;
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
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains submition of uk payment when bad request HTTP status code is returned from bank
 * <p>
 * Covered flows:
 * - 400 Bad Request upon submition of payment
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/hsbcgroup/pis-3.1.6/single/bad-request",
        "classpath:/stubs/hsbcgroup/pis-3.1.6/single/happy-flow/grant-type"}, httpsPort = 0, port = 0)
class HsbcGroupUkDomesticPaymentProviderSubmit400IntegrationTest {

    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
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

    private Stream<PaymentSubmissionProvider> getProviders() {
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
    void shouldReturnResultWithStatusExecutionFailedForConfirmPaymentWhenStatusRejectedFromDomesticPaymentsEndpoint(PaymentSubmissionProvider paymentProvider) throws ConfirmationFailedException {
        // given
        String providerState = """
                {"consentId":"ca0542b33fd34fcbaacbe0ea02d0a0aa","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"2513bfeg\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.10\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"12345678901234\\",\\"Name\\":\\"P. Jantje\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Unstructured\\",\\"Reference\\":\\"MalformedPayment\\"}}"}""";
        SubmitPaymentRequest request = createUkDomesticConfirmPaymentRequest(providerState);

        // when
        PaymentStatusResponseDTO result = paymentProvider.submitPayment(request);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("400 BadRequest");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            [{"ErrorCode":"UK.OBIE.Field.Missing","Message":"Instructed amount does not match","Path":"Data.Initiation.InstructedAmount.Amount","Url":"<url to the api reference for Payment Inititaion API>"}]""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.EXECUTION_FAILED);
                }));
    }

    private SubmitPaymentRequest createUkDomesticConfirmPaymentRequest(String providerState) {
        String authorizationCode = "iVuATHQHLIjtGjeuxtBj6Gfnd8o";
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                "https://www.yolt.com/callback-test/payments/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0#code=" + authorizationCode,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

}