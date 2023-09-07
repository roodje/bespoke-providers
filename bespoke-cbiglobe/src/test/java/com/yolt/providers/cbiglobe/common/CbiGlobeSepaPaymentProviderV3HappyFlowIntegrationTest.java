package com.yolt.providers.cbiglobe.common;

import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.SignerMock;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test suite contains all happy flows for Cbi Globe PIS providers.
 * <p>
 * Covered flows:
 * - initiating payment process
 * - submitting payment process (which involves getting for payment status)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(
        stubs = "classpath:/stubs/pis/3.0/happy_flow",
        httpsPort = 0, port = 0)
@ActiveProfiles("cbiglobe")
public class CbiGlobeSepaPaymentProviderV3HappyFlowIntegrationTest {

    private static final String PAYMENT_ID = "SOME-PAYMENT-ID";
    private static final String PROVIDER_STATE = "SOME-PROVIDER-STATE";

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private RestTemplateManager restTemplateManager;

    private Signer signer = new SignerMock();

    @Autowired
    @Qualifier("PosteItalianeSepaPaymentProviderV3")
    private CbiGlobeSepaPaymentProviderV3 posteItalianePaymentProviderV3;

    @Autowired
    @Qualifier("IntesaSanpaoloSepaPaymentProviderV3")
    private CbiGlobeSepaPaymentProviderV3 intesaSanpaoloPaymentProviderV3;

    private Stream<SepaPaymentProvider> getPaymentProviders() {
        return Stream.of(posteItalianePaymentProviderV3, intesaSanpaoloPaymentProviderV3);
    }

    @BeforeEach
    void initialize() {
        authenticationMeans = new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnLoginUrlAndStateResponseWithProperLoginAndStateForInitiatePaymentWithCorrectData(SepaPaymentProvider paymentProvider) {
        //given
        var sepaInitiatePaymentRequestDTO = prepareInitPaymentRequest()
                .debtorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("IT70R0306948420100000000187")
                        .build())
                .build();
        var initiatePaymentRequest = buildInitiateRequestBuilder(sepaInitiatePaymentRequestDTO);

        //when
        var result = paymentProvider.initiatePayment(initiatePaymentRequest);

        //then
        var uriComponents = UriComponentsBuilder.fromHttpUrl(result.getLoginUrl()).build();
        assertThat(uriComponents.getQueryParams()).isNotEmpty();
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"SOME-PAYMENT-ID"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("RCVD");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnLoginUrlAndStateResponseWithProperLoginAndStateForInitiatePaymentWithoutDebtor(SepaPaymentProvider paymentProvider) {
        //given
        var sepaInitiatePaymentRequestDTO = prepareInitPaymentRequest().build();
        var initiatePaymentRequest = buildInitiateRequestBuilder(sepaInitiatePaymentRequestDTO);

        //when
        var result = paymentProvider.initiatePayment(initiatePaymentRequest);

        //then
        var uriComponents = UriComponentsBuilder.fromHttpUrl(result.getLoginUrl()).build();
        assertThat(uriComponents.getQueryParams()).isNotEmpty();
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"SOME-PAYMENT-ID"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("DAS_I");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnSepaPaymentStatusResponseWithProperPaymentIdAndStatusAcceptedForSubmitPaymentWithCorrectData(SepaPaymentProvider paymentProvider) {
        // given
        var submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?code=123456789&state=state") //not know what will be the int the url
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("""
                        {"paymentId":"SOME-PROVIDER-STATE"}""")
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress("127.0.0.1")
                .setSigner(signer)
                .build();

        // when
        var result = paymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo(PROVIDER_STATE);
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"SOME-PROVIDER-STATE"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("ACSP");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnSepaPaymentStatusResponseWithPaymentIdAndStatusForGetStatusWhenPaymentIdIsProvidedInRequest(SepaPaymentProvider paymentProvider) {
        // given
        var getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId(PAYMENT_ID)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .build();

        // when
        var result = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"SOME-PAYMENT-ID"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("ACSP");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    private static SepaInitiatePaymentRequestDTO.SepaInitiatePaymentRequestDTOBuilder prepareInitPaymentRequest() {
        final DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setCreditorPostalCountry("NL");
        return SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("IT11E03268444900B2860435030")
                        .build())
                .creditorName("Jan Kowalski")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("1.23"))
                        .build())
                .remittanceInformationUnstructured("For beer")
                .dynamicFields(dynamicFields);
    }

    private InitiatePaymentRequest buildInitiateRequestBuilder(final SepaInitiatePaymentRequestDTO sepaInitiatePaymentRequestDTO) {
        return new InitiatePaymentRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setState("fakeState")
                .setSigner(signer)
                .setPsuIpAddress("127.0.0.1")
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setRequestDTO(sepaInitiatePaymentRequestDTO)
                .build();
    }
}
