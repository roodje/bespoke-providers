package com.yolt.providers.stet.societegeneralegroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.stet.SignerMock;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.stet.societegeneralegroup.common.auth.SocieteGeneraleAuthenticationMeansSupplier.*;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = SocieteGeneraleTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/societegenerale/pis/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("societegenerale")
class SocieteGeneralePaymentProviderIntegrationTest {

    private static final String CLIENT_SECRET = "55ffxxxx7eeaxxxx8dc8xxxx4d61xxxx";
    private static final UUID TRANSPORT_KEY_ID_ROTATION = UUID.randomUUID();
    private static final UUID SIGNING_KEY_ID_ROTATION = UUID.randomUUID();

    private static final String REDIRECT_URL = "https://www.redirect.url/callback/payment";
    private static final String CLIENT_ID = "f330xxxx6fe8xxxx6edbxxxxd15axxxx";
    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = prepareMeans();

    @Value("${wiremock.server.port}")
    private int port;

    @Autowired
    @Qualifier("SocieteGeneraleProPaymentProviderV5")
    private SepaPaymentProvider proPaymentProvider;

    @Autowired
    @Qualifier("SocieteGeneralePriPaymentProviderV5")
    private SepaPaymentProvider priPaymentProvider;

    @Autowired
    @Qualifier("SocieteGeneraleEntPaymentProviderV5")
    private SepaPaymentProvider entPaymentProvider;

    private Stream<SepaPaymentProvider> getAllSocieteGeneralePaymentProviders() {
        return Stream.of(
                proPaymentProvider,
                priPaymentProvider,
                entPaymentProvider);
    }

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    private RestTemplateManager restTemplateManager = new SimpleRestTemplateManagerMock();

    private Signer signer = new SignerMock();

    @ParameterizedTest
    @MethodSource("getAllSocieteGeneralePaymentProviders")
    void shouldReturnProperMapOfAuthenticationMeans(SepaPaymentProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthenticationMeans).containsOnlyKeys(CLIENT_ID_NAME, CLIENT_SECRET_NAME, CLIENT_SIGNING_KEY_ID_ROTATION,
                CLIENT_SIGNING_CERTIFICATE_ROTATION, CLIENT_TRANSPORT_KEY_ID_ROTATION, CLIENT_TRANSPORT_CERTIFICATE_ROTATION);
    }

    @ParameterizedTest
    @MethodSource("getAllSocieteGeneralePaymentProviders")
    void shouldInitiatePaymentAndReturnLoginUrl(SepaPaymentProvider paymentProvider) throws JsonProcessingException {
        // given
        InitiatePaymentRequest request = new InitiatePaymentRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setRequestDTO(createPaymentRequest("124.35"))
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState("state")
                .setAuthenticationMeans(AUTH_MEANS)
                .setSigner(signer)
                .build();
        // when
        LoginUrlAndStateDTO loginUrlAndStateDTO = paymentProvider.initiatePayment(request);
        //then
        assertThat(loginUrlAndStateDTO.getLoginUrl()).isEqualTo("https://particuliers.societegenerale.fr/app/auth/icd/obu/index-authsec.html#obu/eaefdeff-11ec-429f-bb55-019245fe0604?usuallyAbsentQueryParam=true");
        assertThat(loginUrlAndStateDTO.getProviderState()).isEqualTo("{\"paymentId\":\"eaefdeff-11ec-429f-bb55-019245fe0604\"}");
        assertThat(loginUrlAndStateDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("UNKNOWN");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                });
    }

    @ParameterizedTest
    @MethodSource("getAllSocieteGeneralePaymentProviders")
    public void shouldSubmitPaymentAndReturnPaymentStatusAndId(SepaPaymentProvider paymentProvider) throws JsonProcessingException {
        // given
        SubmitPaymentRequest request = new SubmitPaymentRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(AUTH_MEANS)
                .setSigner(signer)
                .setProviderState("{\"paymentId\":\"eaefdeff-11ec-429f-bb55-019245fe0604\"}")
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL)
                .build();
        // when
        SepaPaymentStatusResponseDTO responseDTO = paymentProvider.submitPayment(request);
        //then
        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo("eaefdeff-11ec-429f-bb55-019245fe0604");
        assertThat(responseDTO.getProviderState()).isEqualTo("{\"paymentId\":\"eaefdeff-11ec-429f-bb55-019245fe0604\"}");
        assertThat(responseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.RCVD.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                });
    }

    @ParameterizedTest
    @MethodSource("getAllSocieteGeneralePaymentProviders")
    public void shouldReturnPaymentStatusResponseWithPaymentIdAndStatusForGetStatusWhenPaymentIdIsProvidedInRequest(SepaPaymentProvider sepaPaymentProvider) {
        // given
        GetStatusRequest request = new GetStatusRequestBuilder()
                .setPaymentId("eaefdeff-11ec-429f-bb55-019245fe0604")
                .setProviderState("{\"paymentId\":\"eaefdeff-11ec-429f-bb55-019245fe0604\"}")
                .setSigner(signer)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        SepaPaymentStatusResponseDTO responseDTO = sepaPaymentProvider.getStatus(request);

        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo("eaefdeff-11ec-429f-bb55-019245fe0604");
        assertThat(responseDTO.getProviderState()).isEqualTo("{\"paymentId\":\"eaefdeff-11ec-429f-bb55-019245fe0604\"}");
        assertThat(responseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.ACSC.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                });
    }

    @ParameterizedTest
    @MethodSource("getAllSocieteGeneralePaymentProviders")
    public void shouldReturnPaymentStatusResponseWithPaymentIdAndStatusForGetStatusWhenPaymentIdIsNotProvidedInRequest(SepaPaymentProvider sepaPaymentProvider) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId(null)
                .setProviderState("{\"paymentId\":\"eaefdeff-11ec-429f-bb55-019245fe0604\"}")
                .setSigner(signer)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        SepaPaymentStatusResponseDTO responseDTO = sepaPaymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo("eaefdeff-11ec-429f-bb55-019245fe0604");
        assertThat(responseDTO.getProviderState()).isEqualTo("{\"paymentId\":\"eaefdeff-11ec-429f-bb55-019245fe0604\"}");
        assertThat(responseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.ACSC.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                });
    }

    private SepaInitiatePaymentRequestDTO createPaymentRequest(final String amount) {
        return SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("FR7630006000011234567890189")
                        .build())
                .creditorName("Buzz Lightyear")
                .endToEndIdentification("4ccb2450-eeb1-11ea-3333-0242ac120002")
                .executionDate(LocalDate.now().plusDays(10L))
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal(amount))
                        .build())
                .instructionPriority(InstructionPriority.NORMAL)
                .remittanceInformationUnstructured("To infinity and beyond!")
                .build();
    }

    private static Map<String, BasicAuthenticationMean> prepareMeans() {
        Map<String, BasicAuthenticationMean> means = new HashMap<>();
        means.put(CLIENT_ID_NAME, new BasicAuthenticationMean(API_KEY.getType(), CLIENT_ID));
        means.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(API_SECRET.getType(), CLIENT_SECRET));
        means.put(CLIENT_TRANSPORT_KEY_ID_ROTATION, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID_ROTATION.toString()));
        means.put(CLIENT_TRANSPORT_CERTIFICATE_ROTATION, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(),
                readCertificate("certificates/societegenerale/yolt_certificate_transport.pem")));
        means.put(CLIENT_SIGNING_KEY_ID_ROTATION, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID_ROTATION.toString()));
        means.put(CLIENT_SIGNING_CERTIFICATE_ROTATION, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(),
                readCertificate("certificates/societegenerale/yolt_certificate_signing.pem")));
        return means;
    }

    private static String readCertificate(String certificatePath) {
        try {
            URI fileURI = SocieteGeneralePaymentProviderIntegrationTest.class
                    .getClassLoader()
                    .getResource(certificatePath)
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
