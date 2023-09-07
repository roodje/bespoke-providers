package com.yolt.providers.openbanking.ais.nationwide.pis.v12;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.nationwide.NationwideApp;
import com.yolt.providers.openbanking.ais.nationwide.NationwideSampleAuthenticationMeans;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains creation of uk payment when payment response is lack of consent ID.
 * <p>
 * Disclaimer: Nationwide is a single bank, so there is no need to parametrize this test class.
 * <p>
 * Covered flows:
 * - Missing consent ID in response from bank for creation of payment
 * <p>
 */
@SpringBootTest(classes = {NationwideApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/nationwide/pis-3.1.2/payment-missing-data", httpsPort = 0, port = 0)
@ActiveProfiles("nationwide")
class NationwideUkDomesticPaymentProviderV12MissingConsentIdInResponseIntegrationTest {

    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");

    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;

    @Autowired
    @Qualifier("NationwidePaymentProviderV12")
    private GenericBasePaymentProviderV2 paymentProvider;

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        authenticationMeans = new NationwideSampleAuthenticationMeans().getAuthenticationMeans();
        signer = new SignerMock();
    }

    @Test
    void shouldReturnResultWithStatusInitiationErrorAndRawBodyResponseAsReasonForCreatePaymentWhenPaymentIdIsNotProvidedInBankResponse() {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );

        // when
        InitiateUkDomesticPaymentResponseDTO result = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            {"Data":{"CreationDateTime":"2020-12-03T10:22:46.077Z","Status":"Rejected","StatusUpdateDateTime":"2020-12-03T10:22:46.077Z","Initiation":{"InstructionIdentification":"20201203108401220-50afa69f-ad6","EndToEndIdentification":"FEF32557","InstructedAmount":{"Amount":"0.01","Currency":"GBP"},"DebtorAccount":{"SchemeName":"UK.OBIE.IBAN","Identification":"8272908780568576","Name":"Alex Mitchell"},"CreditorAccount":{"SchemeName":"UK.OBIE.IBAN","Identification":"1802968485593088","Name":"Jordan Bell"},"RemittanceInformation":{"Unstructured":"Payment"}}},"Risk":{"PaymentContextCode":"Other"},"Links":{"Self":"https://api.natwest.com/open-banking/v3.1/pisp/domestic-payment-consents/331d76df48ed41229b67f062dd55e340"},"Meta":{"TotalPages":1}}""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO() {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", AccountIdentifierScheme.IBAN, "Alex Mitchell", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.IBAN, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "FEF32557",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                creditorAccount,
                debtorAccount,
                "Payment",
                new HashMap<>()
        );
    }
}