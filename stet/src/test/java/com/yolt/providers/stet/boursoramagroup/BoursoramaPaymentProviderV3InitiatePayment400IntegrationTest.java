package com.yolt.providers.stet.boursoramagroup;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.stet.generic.GenericPaymentProviderV2;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus.INITIATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This test contains flow for scenarios when 4xx error
 * occurs during payment initiation process
 * In such case we assume payment is not properly created/initiated and we throw {@link CreationFailedException}.
 * Tests is parametrized and run for all providers in group
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = BoursoramaGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("boursorama")
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/boursorama/pis/initiate-payment-400",
        "classpath:/stubs/boursorama/pis/happy-flow/token"}, httpsPort = 0, port = 0)
public class BoursoramaPaymentProviderV3InitiatePayment400IntegrationTest {

    private static final String BASE_CLIENT_REDIRECT_URL = "https://www.yolt.com/callback/payment";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String STATE = "state";

    private final Signer signer = mock(Signer.class);
    private final Map<String, BasicAuthenticationMean> authenticationMeans = BoursoramaGroupSampleMeans.getAuthMeans();

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("BoursoramaPaymentProviderV3")
    private GenericPaymentProviderV2 boursoramaPaymentProviderV3;

    Stream<SepaPaymentProvider> getBoursoramaProviders() {
        return Stream.of(boursoramaPaymentProviderV3);
    }

    @BeforeEach
    public void setUp() {
        when(signer.sign(ArgumentMatchers.any(byte[].class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(Base64.toBase64String("TEST-ENCODED-SIGNATURE".getBytes()));
    }

    @ParameterizedTest
    @MethodSource("getBoursoramaProviders")
    public void shouldReturnEmptyLoginUrlAndEmptyProviderStateAndProperStatusesInPecMetadataForInitiatePaymentWhen4xxError(SepaPaymentProvider paymentProviderUnderTest) {
        // given
        DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setDebtorName("John Debtor");

        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL62ABNA9999841479"))
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7640618802500004082626224"))
                .creditorName("myMerchant")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("124.35")))
                .remittanceInformationUnstructured("Motif du virement")
                .executionDate(LocalDate.of(2020, 1, 1))
                .dynamicFields(dynamicFields)
                .build();

        InitiatePaymentRequest request = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        LoginUrlAndStateDTO loginUrlAndStateDTO = paymentProviderUnderTest.initiatePayment(request);

        // then
        assertThat(loginUrlAndStateDTO.getLoginUrl()).isEmpty();
        assertThat(loginUrlAndStateDTO.getProviderState()).isEmpty();
        assertThat(loginUrlAndStateDTO.getPaymentExecutionContextMetadata())
                .satisfies(pecMetadata -> assertThat(pecMetadata.getPaymentStatuses())
                        .extracting(
                                statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("Bad Request", "The format of the input is not valid", INITIATION_ERROR));
    }
}
