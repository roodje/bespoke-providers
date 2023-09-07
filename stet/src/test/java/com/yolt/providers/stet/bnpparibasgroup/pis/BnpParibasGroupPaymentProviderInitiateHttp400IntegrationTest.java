package com.yolt.providers.stet.bnpparibasgroup.pis;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupTestConfig;
import com.yolt.providers.stet.bnpparibasgroup.common.configuration.BnpParibasGroupSampleAuthenticationMeans;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
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
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus.INITIATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = BnpParibasGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bnpparibasgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/bnpparibasgroup/pis/initiate-400", httpsPort = 0, port = 0)
public class BnpParibasGroupPaymentProviderInitiateHttp400IntegrationTest {

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Signer signerMock;

    @Autowired
    @Qualifier("BnpParibasPaymentProviderV2")
    private GenericPaymentProviderV3 bnpParibasPaymentProviderV2;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        authenticationMeans = new BnpParibasGroupSampleAuthenticationMeans().getBnpSampleAuthenticationMeans();
    }

    Stream<SepaPaymentProvider> getBnpParibasGroupPaymentProviders() {
        return Stream.of(bnpParibasPaymentProviderV2);
    }

    @ParameterizedTest
    @MethodSource("getBnpParibasGroupPaymentProviders")
    void shouldInitiatePayment(SepaPaymentProvider provider) {
        // given
        DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setDebtorName("John Debtor");

        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL66ABNA9999841234"))
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7630004030160000003778069"))
                .creditorName("myMerchant")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("124.35")))
                .remittanceInformationUnstructured("MyRemittanceInformation")
                .executionDate(LocalDate.now())
                .dynamicFields(dynamicFields)
                .build();
        String baseClientRedirectUrl = "https://www.yolt.com/callback/payment";
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setSigner(signerMock)
                .setBaseClientRedirectUrl(baseClientRedirectUrl)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setState("state")
                .build();

        // when
        LoginUrlAndStateDTO result = provider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata())
                .satisfies(pecMetadata -> assertThat(pecMetadata.getPaymentStatuses())
                        .extracting(
                                statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("Bad Request", "Missing request header 'Digest' for method parameter of type String", INITIATION_ERROR));
    }
}
