package com.yolt.providers.openbanking.ais.santander.pis;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.santander.SantanderApp;
import com.yolt.providers.openbanking.ais.santander.SantanderSampleAuthenticationMeansV2;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.yolt.providers.common.Constants.CLIENT_ID_YOLT;
import static com.yolt.providers.common.Constants.CLIENT_REDIRECT_URL_ID_YOLT_APP;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains creation of uk payment when payment response is lack of consent ID.
 * <p>
 * Disclaimer: Santader is a single bank, so there is no need to parametrize this test class.
 * <p>
 * Covered flows:
 * - Missing consent ID in response from bank for creation of payment
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SantanderApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/santander/pis-3.1.19/missing-data", httpsPort = 0, port = 0)
@ActiveProfiles("santander")
class SantanderUkDomesticPaymentProviderMissingDataIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback?code=";
    private static final Signer SIGNER = new SignerMock();

    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);
    private static final String TEST_PSU_IP_ADDRESS = "12.34.56.78";
    private static final String TEST_STATE = "6d1d0bd4-2536-467e-9bdf-a69ef6c21a65";

    private RestTemplateManagerMock restTemplateManagerMock;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("SantanderPaymentProviderV15")
    private GenericBasePaymentProviderV2 santanderPaymentProviderV15;

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "1626df30-50ad-42d8-8f39-40dd95f4b15f");
        authenticationMeans = new SantanderSampleAuthenticationMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnResultWithStatusInitiationErrorAndRawBodyResponseAsReasonForCreatePaymentWhenPaymentIdIsNotProvidedInBankResponse(GenericBasePaymentProviderV2 provider) {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );

        // when
        InitiateUkDomesticPaymentResponseDTO result = provider.initiateSinglePayment(request);

        // then
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            {"Data":{"Status":"Rejected","CreationDateTime":"2012-03-08T07:26:20.745Z","StatusUpdateDateTime":"2019-10-29T13:38:04Z","Initiation":{"InstructionIdentification":"1938883646324736","EndToEndIdentification":"2707380502528000","InstructedAmount":{"Amount":"100.0","Currency":"JPY"},"CreditorAccount":{"SchemeName":"UK.OBIE.SortCodeAccountNumber","Identification":"1802968485593088","Name":"Jordan Bell"},"RemittanceInformation":{"Unstructured":"onmobhetelalobucmogigelecarreocukaezeji"}}},"Risk":{"PaymentContextCode":"PartyToParty"},"Links":{"Self":"https://openbanking-ma.santander.co.uk/sanuk/external/open-banking/v3.1/pisp/domestic-payment-consents/4851833560367104"},"Meta":{}}""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO() {
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "2707380502528000",
                CurrencyCode.JPY.toString(),
                new BigDecimal("100.0"),
                creditorAccount,
                null,
                "onmobhetelalobucmogigelecarreocukaezeji",
                new HashMap<>()
        );
    }

    private Stream<GenericBasePaymentProviderV2> getPaymentProviders() {
        return Stream.of(santanderPaymentProviderV15);
    }
}