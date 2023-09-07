package com.yolt.providers.openbanking.ais.tsbgroup.pis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.tsbgroup.TsbGroupApp;
import com.yolt.providers.openbanking.ais.tsbgroup.TsbGroupSampleTypedAuthenticationMeans;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeAll;
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
 * This test contains all payment happy flows occuring in TSB.
 * <p>
 * Disclaimer: as all providers in TSB group are the same from code and stubs perspective (then only difference is configuration)
 * we are running parametrized tests for testing, but this covers all providers from TSB group.
 * <p>
 * Covered flows:
 * - successful creation of payment
 * - successful return of consent page url
 * - successful confirmation of payment
 * - confirming UK domestic payment
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TsbGroupApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("tsbgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/tsbgroup/pis/ob_3.1.1/happy-flow", httpsPort = 0, port = 0)
public class TsbPaymentProviderIntegrationTest {

    private static final UUID CLIENT_ID = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static Map<String, BasicAuthenticationMean> authenticationMeans;
    private static RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("TsbPaymentProviderV5")
    private GenericBasePaymentProviderV2 tsbPaymentProviderV5;

    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID, CLIENT_REDIRECT_URL_ID);

    private Stream<GenericBasePaymentProviderV2> getTsbPaymentProviders() {
        return Stream.of(tsbPaymentProviderV5);
    }

    @BeforeAll
    public static void beforeAll() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "87da2798-f7e2-4823-80c1-3c03344b8f13");

        final ProviderAccountNumberDTO creditorAccount = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER, "12345678901234");
        creditorAccount.setHolderName("P. Jantje");

        ProviderAccountNumberDTO debtor = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER, "3238619340865536");

        debtor.setHolderName("Owen McDaniel");

        authenticationMeans = new TsbGroupSampleTypedAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getTsbPaymentProviders")
    public void shouldReturnResponseWithLoginUrlAndProviderStateAndInitiationSuccessStatusInPecMetadataForInitiateSinglePaymentWhenCorrectDataAreProvided(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        // given
        UkAccountDTO creditorAccount = new UkAccountDTO("12345678901234",
                AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                "P. Jantje",
                null);
        InitiateUkDomesticPaymentRequestDTO requestDTO = new InitiateUkDomesticPaymentRequestDTO(
                "35B64F93",
                String.valueOf(CurrencyCode.GBP),
                new BigDecimal("0.01"),
                creditorAccount,
                null,
                "SomeRandomMessage",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "SomeRandomMessage2")
        );

        String state = UUID.randomUUID().toString();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                "https://www.yolt.com/callback/payment",
                state,
                authenticationMeans,
                new SignerMock(),
                restTemplateManagerMock,
                null,
                authenticationMeansReference
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(response.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=someClientId")
                .contains("state=" + state)
                .contains("scope=openid+payments")
                .contains("nonce=" + state)
                .contains("redirect_uri=https%3A%2F%2Fwww.yolt.com%2Fcallback%2Fpayment")
                .contains("request=");
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getTsbPaymentProviders")
    public void shouldReturnResponseWithPaymentIdAndAcceptedStatusInPecMetadataForSubmitPaymentWhenCorrectDataAreProvided(PaymentSubmissionProvider paymentProvider) throws ConfirmationFailedException {
        // given
        String providerState = """
                {"consentId":"bec2bc664f984571b5a20ea666a7d0c1","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"2513bfeg\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"12345678901234\\",\\"Name\\":\\"P. Jantje\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"SomeRandomMessage\\",\\"Reference\\":\\"SomeRandomMessage2\\"},\\"Risk\\":{\\"PaymentContextCode\\":\\"PartyToParty\\"}}"}""";
        SubmitPaymentRequest request = new SubmitPaymentRequest(providerState,
                authenticationMeans,
                "https://www.yolt.com/callback/payment#code=fakeAuthCode",
                new SignerMock(),
                restTemplateManagerMock,
                null,
                authenticationMeansReference);

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo("e23f5d5cd08d44c3993243ad3f19d56e");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementInProcess");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getTsbPaymentProviders")
    public void shouldReturnResponseWithPaymentIdAndCompletedStatusInPecMetadataForGetStatusWhenPaymentIdIsProvidedInRequest(PaymentSubmissionProvider paymentProvider) {
        // given
        GetStatusRequest request = createGetStatusRequest(true);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo("e23f5d5cd08d44c3993243ad3f19d56e");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementCompleted");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getTsbPaymentProviders")
    public void shouldReturnResponseWithConsentIdAndInitiationSuccessStatusInPecMetadataForGetStatusWhenPaymentIdIsNotProvidedInRequest(PaymentSubmissionProvider paymentProvider) throws JsonProcessingException {
        // given
        GetStatusRequest request = createGetStatusRequest(false);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(request);

        // then
        assertThat(response.getPaymentId()).isEmpty();

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("GENERIC-P-PAYMENT_ID", PaymentType.SINGLE, """
                        {"Status":"AwaitingAuthorisation","resourceId":"GENERIC-P-PAYMENT_ID"}""");

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    private GetStatusRequest createGetStatusRequest(boolean withPaymentId) {
        return new GetStatusRequest(withPaymentId ? null : createUkProviderState(new UkProviderState("GENERIC-P-PAYMENT_ID", PaymentType.SINGLE, null)),
                withPaymentId ? "e23f5d5cd08d44c3993243ad3f19d56e" : null,
                authenticationMeans,
                new SignerMock(),
                restTemplateManagerMock,
                null,
                authenticationMeansReference);
    }

    private static String createUkProviderState(UkProviderState ukProviderState) {
        try {
            return new ObjectMapper().writeValueAsString(ukProviderState);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
