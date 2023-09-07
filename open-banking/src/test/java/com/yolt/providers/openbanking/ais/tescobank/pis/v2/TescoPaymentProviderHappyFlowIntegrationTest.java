package com.yolt.providers.openbanking.ais.tescobank.pis.v2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.exception.GeneralConfirmException;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.tescobank.TescoBankApp;
import com.yolt.providers.openbanking.ais.tescobank.TescoSampleTypedAuthenticationMeansV2;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all payment happy flows occurring in provider.
 * <p>
 * Covered flows:
 * - successful return of consent page url
 * - successful creation of payment
 * - successful confirmation of payment
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TescoBankApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/tescobank/pis-3.1/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("tescobank")
public class TescoPaymentProviderHappyFlowIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String AUTHORIZATION_CODE = "?code=fakeAuthCode";
    private static final String STUBBED_PAYMENT_AUTHORIZE_URL = "https://localhost:(.*)/authorize\\?response_type=code\\+id_token&client_id=(.*)&state=(.*)&scope=openid\\+payments&nonce=(.*)&redirect_uri=(.*)&request=(.*)";
    private static final String REQUEST_TRACE_ID = UUID.randomUUID().toString();
    private static final UUID CLIENT_ID = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final AuthenticationMeansReference AUTHENTICATION_MEANS_REFERENCE = new AuthenticationMeansReference(CLIENT_ID, CLIENT_REDIRECT_URL_ID);

    private static final Signer SIGNER = new SignerMock();

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("TescoBankPaymentProviderV5")
    private GenericBasePaymentProviderV2 tescoBankPaymentProviderV5;


    private Stream<GenericBasePaymentProviderV2> getProviders() {
        return Stream.of(tescoBankPaymentProviderV5);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = TescoSampleTypedAuthenticationMeansV2.getTypedAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> REQUEST_TRACE_ID);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithLoginUrlAndProviderStateAndInitiationSuccessStatusInPecMetadataForInitiateSinglePaymentWhenCorrectDataAreProvided(UkDomesticPaymentProvider provider) throws CreationFailedException {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = createUkPaymentRequest();
        InitiateUkDomesticPaymentRequest paymentRequest = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                "test",
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                null,
                AUTHENTICATION_MEANS_REFERENCE
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = provider.initiateSinglePayment(paymentRequest);

        // then
        assertThat(response.getLoginUrl()).matches(STUBBED_PAYMENT_AUTHORIZE_URL);
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithPaymentIdAndAcceptedStatusInPecMetadataForSubmitPaymentWhenCorrectDataAreProvided(PaymentSubmissionProvider paymentProvider) throws GeneralConfirmException {
        // given
        String state = """
                {"consentId":"TESCOBANK-P-PAYMENT_ID","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20201203108401220-50afa69f-ad6\\",\\"EndToEndIdentification\\":\\"B7F2761C\\",\\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"GBP\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"45654645435345\\",\\"Name\\":\\"Jordan Bell\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"20581634112471\\",\\"Name\\":\\"Robin Hood\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Unstructured\\"}}"}""";
        SubmitPaymentRequest request = new SubmitPaymentRequest(
                state,
                authenticationMeans,
                REDIRECT_URL + AUTHORIZATION_CODE,
                SIGNER,
                restTemplateManagerMock,
                null,
                AUTHENTICATION_MEANS_REFERENCE
        );

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo("PAYMENT_SUBMISSION_ID");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Pending");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithPaymentIdAndCompletedStatusInPecMetadataForGetStatusWhenCorrectDataAreProvided(PaymentSubmissionProvider paymentProvider) {
        // given
        GetStatusRequest request = new GetStatusRequest(null,
                "PAYMENT_SUBMISSION_ID",
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                null,
                AUTHENTICATION_MEANS_REFERENCE);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo("PAYMENT_SUBMISSION_ID");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedCreditSettlementCompleted");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createUkPaymentRequest() {
        return new InitiateUkDomesticPaymentRequestDTO(
                "B7F2761C",
                CurrencyCode.GBP.toString(),
                new BigDecimal("10000.00"),
                new UkAccountDTO("20581634112471", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Robin Hood", null),
                new UkAccountDTO("45654645435345", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null),
                "Unstructured",
                Collections.emptyMap()
        );
    }
}