package com.yolt.providers.openbanking.ais.virginmoney.pisv3;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneyApp;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneyJwsSigningResult;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneySampleAuthenticationMeansV3;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
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

import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test suite contains happy flows occurring in Virgin Money provider.
 * Covered flows:
 * - create payment
 * <p>
 * TODO: all this tests needs to be adjusted once after obtaining some real anonymised data C4PO-8062
 */
@SpringBootTest(classes = {VirginMoneyApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("virginmoney")
@AutoConfigureWireMock(stubs = "classpath:/stubs/virginmoney/pis/v2/happyflow", httpsPort = 0, port = 0)
public class VirginMoneyDomesticPaymentV4HappyFlowIntegrationTest {
    private static final RestTemplateManager REST_TEMPLATE_MANAGER = new RestTemplateManagerMock(() -> "12345");
    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");

    @Autowired
    @Qualifier("VirginMoneyPaymentProviderV4")
    private GenericBasePaymentProvider paymentProvider;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private AuthenticationMeansReference authenticationMeansReference;
    private VirginMoneySampleAuthenticationMeansV3 sampleAuthenticationMeans = new VirginMoneySampleAuthenticationMeansV3();

    @Mock
    private Signer signer;


    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = sampleAuthenticationMeans.getVirginMoneySampleAuthenticationMeansForPis();
        authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), any()))
                .thenReturn(new VirginMoneyJwsSigningResult());
    }


    @Test
    public void shouldCreateUkDomesticPayment() throws CreationFailedException {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                signer,
                REST_TEMPLATE_MANAGER,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(response.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=someClientId")
                .contains("state=" + TEST_STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + TEST_STATE)
                .contains("redirect_uri=https%3A%2F%2Fyolt.com%2Fcallback-test")
                .contains("request=");
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO() {
        UkAccountDTO debtorAccount = new UkAccountDTO("08606467451219", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Mr Robin Hood", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("60132378234512", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Batman", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "3482044854173696",
                CurrencyCode.JPY.toString(),
                new BigDecimal("220.50"),
                creditorAccount,
                debtorAccount,
                "Unstructured",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "Structured")
        );
    }
}
