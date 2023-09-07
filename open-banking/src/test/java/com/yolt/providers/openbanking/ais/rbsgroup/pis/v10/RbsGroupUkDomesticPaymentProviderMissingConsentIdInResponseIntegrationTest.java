package com.yolt.providers.openbanking.ais.rbsgroup.pis.v10;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
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
 * This test contains creation of uk payment when payment response is lack of consent ID.
 * <p>
 * Covered flows:
 * - Missing consent ID in response from bank for creation of payment
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {RbsApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/ob_3.1.6/pis", httpsPort = 0, port = 0)
class RbsGroupUkDomesticPaymentProviderMissingConsentIdInResponseIntegrationTest {

    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final String INVALID_CREDITOR_NUMBER = "98765432109876";
    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;

    @Autowired
    @Qualifier("NatWestPaymentProviderV11")
    private GenericBasePaymentProviderV2 natWestPaymentProviderV11;

    @Autowired
    @Qualifier("RoyalBankOfScotlandPaymentProviderV11")
    private GenericBasePaymentProviderV2 royalBankOfScotlandPaymentProviderV11;

    @Autowired
    @Qualifier("UlsterBankPaymentProviderV10")
    private GenericBasePaymentProviderV2 ulsterBankPaymentProviderV10;

    private Stream<UkDomesticPaymentProvider> getPecAwareProviders() {
        return Stream.of(natWestPaymentProviderV11,
                royalBankOfScotlandPaymentProviderV11,
                ulsterBankPaymentProviderV10);
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        authenticationMeans = RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForPis();
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getPecAwareProviders")
    void shouldReturnResultWithStatusInitiationErrorAndRawBodyResponseAsReasonForCreatePaymentWhenPaymentIdIsNotProvidedInBankResponse(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO(INVALID_CREDITOR_NUMBER);
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
                            {"Data":{"CreationDateTime":"2019-08-20T13:07:51.710Z","Status":"Rejected","StatusUpdateDateTime":"2019-08-20T13:07:51.710Z","Initiation":{"InstructionIdentification":"h3ce0bdd-3460-48c0-ae63-b0981","EndToEndIdentification":"524a62e9-d","InstructedAmount":{"Amount":"0.01","Currency":"GBP"},"CreditorAccount":{"SchemeName":"UK.OBIE.SortCodeAccountNumber","Identification":"41141266529926","Name":"John Doe"},"RemittanceInformation":{"Reference":"Unstructured"}}},"Risk":{"PaymentContextCode":"PartyToParty"},"Links":{"Self":"https://api.natwest.com/open-banking/v3.1/pisp/domestic-payment-consents/331d76df48ed41229b67f062dd55e340"},"Meta":{"TotalPages":1}}""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO(String identification) {
        UkAccountDTO creditorAccount = new UkAccountDTO(identification, AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "P. Jantje", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "35B64F94",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                creditorAccount,
                null,
                "SomeRandomMessage",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "SomeRandomMessage2")
        );
    }

}