package com.yolt.providers.stet.generic.mapper.payment;

import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import lombok.SneakyThrows;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static nl.ing.lovebird.extendeddata.common.CurrencyCode.EUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
class DefaultPaymentMapperTest {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
    private static final String STATE = "174d3210-0497-4181-b2fb-c1798a2b777f";
    private static final String PSU_IP_ADDRESS = "128.0.0.1";
    private static final String BASE_CLIENT_REDIRECT_URL = "https://yolt.com/payment";
    private static final String REDIRECT_URL_WITH_STATE = BASE_CLIENT_REDIRECT_URL + "?state=" + STATE;
    private static final String REDIRECT_URL_WITH_STATE_AND_ERROR = REDIRECT_URL_WITH_STATE + "&error=wrong";

    private DefaultPaymentMapper paymentMapper;

    @BeforeEach
    void initialize() {
        paymentMapper = new DefaultPaymentMapper(new DateTimeSupplier(Clock.systemUTC()));
    }

    @Test
    void shouldMapToPaymentRequestResource() {
        // given
        InitiatePaymentRequest request = createInitiatePaymentRequest(BASE_CLIENT_REDIRECT_URL);
        DefaultAuthenticationMeans authMeans = createDefaultAuthenticationMeans();

        // when
        StetPaymentInitiationRequestDTO paymentInitiationRequestDTO = paymentMapper.mapToStetPaymentInitiationRequestDTO(request, authMeans);

        // then
        assertThat(paymentInitiationRequestDTO).satisfies(validatePaymentRequestResource(request));
    }

    @Test
    void shouldMapToAmountType() {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = createSepaInitiatePaymentRequestDTO();

        // when
        StetAmountTypeDTO instructedAmount = paymentMapper.mapToInstructedAmount(requestDTO);

        // then
        assertThat(instructedAmount.getAmount()).isEqualTo(10.50F);
        assertThat(instructedAmount.getCurrency()).isEqualTo(EUR.name());
    }

    @Test
    void shouldMapToAccountIdentification() {
        // given
        SepaAccountDTO accountDTO = createSepaAccountDTO("FR3030003000707489581895O90");

        // when
        StetAccountIdentificationDTO accountIdentificationDTO = paymentMapper.mapToAccountIdentification(accountDTO);

        // then
        assertThat(accountIdentificationDTO.getIban()).isEqualTo(accountDTO.getIban());
        assertThat(accountIdentificationDTO.getCurrency()).isEqualTo(EUR.name());
    }

    @Test
    void shouldNotMapToAccountIdentificationDueToMissingAccountDTO() {
        // given
        SepaAccountDTO accountDTO = null;

        // when
        StetAccountIdentificationDTO accountIdentificationDTO = paymentMapper.mapToAccountIdentification(accountDTO);

        // then
        assertThat(accountIdentificationDTO).isNull();
    }

    @Test
    void shouldMapToPartyIdentification() {
        // given
        String partyName = "PartyName";

        // when
        StetPartyIdentificationDTO partyIdentificationDTO = paymentMapper.mapToPartyIdentification(partyName);

        // then
        assertThat(partyIdentificationDTO.getName()).isEqualTo(partyName);
    }

    @SneakyThrows
    @Test
    void shouldMapToSupplementaryData() {
        // given
        InitiatePaymentRequest request = createInitiatePaymentRequest(BASE_CLIENT_REDIRECT_URL);

        // when
        StetSupplementaryDataDTO supplementaryData = paymentMapper.mapToSupplementaryData(request);

        // then
        assertThat(supplementaryData.getSuccessfulReportUrl()).isEqualTo(URLEncoder.encode(REDIRECT_URL_WITH_STATE, StandardCharsets.UTF_8));
        assertThat(supplementaryData.getUnsuccessfulReportUrl()).isEqualTo(URLEncoder.encode(REDIRECT_URL_WITH_STATE_AND_ERROR, StandardCharsets.UTF_8));
        assertThat(supplementaryData.getAppliedAuthenticationApproach()).isEqualTo(StetAuthenticationApproach.REDIRECT);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenMappingToSupplementaryDataDueToMissingRedirectUrl() {
        // given
        String baseClientRedirectUrl = "";
        InitiatePaymentRequest request = createInitiatePaymentRequest(baseClientRedirectUrl);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> paymentMapper.mapToSupplementaryData(request);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldMapToRemittanceInformation() {
        // given
        SepaInitiatePaymentRequestDTO initiatePaymentRequestDTO = createSepaInitiatePaymentRequestDTO();

        // when
        StetRemittanceInformationDTO remittanceInformationDTO = paymentMapper.mapToRemittanceInformation(initiatePaymentRequestDTO);

        // then
        assertThat(remittanceInformationDTO.getUnstructured()).containsExactly("RemittanceInformationUnstructured");
    }

    @Test
    void shouldMapToPaymentIdentification() {
        // given
        SepaInitiatePaymentRequestDTO initiatePaymentRequestDTO = createSepaInitiatePaymentRequestDTO();

        // when
        StetPaymentIdentificationDTO paymentIdentificationDTO = paymentMapper.mapToPaymentIdentification(initiatePaymentRequestDTO);

        // then
        assertThat(paymentIdentificationDTO.getInstructionId()).matches("[a-f0-9]{32}");
        assertThat(paymentIdentificationDTO.getEndToEndId()).isEqualTo("EndToEndIdentification");
    }

    @Test
    void shouldCreatePaymentTypeInformation() {
        // given-when
        StetPaymentTypeInformationDTO paymentTypeInformationDTO = paymentMapper.createPaymentTypeInformation();

        // then
        assertThat(paymentTypeInformationDTO.getServiceLevel()).isEqualTo(StetServiceLevel.SEPA);
        assertThat(paymentTypeInformationDTO.getCategoryPurpose()).isEqualTo(StetCategoryPurpose.CASH);
        assertThat(paymentTypeInformationDTO.getLocalInstrument()).isNull();
        assertThat(paymentTypeInformationDTO.getInstructionPriority()).isNull();
    }

    @ParameterizedTest
    @CsvSource({"ACCP,ACCEPTED", "ACSC,ACCEPTED", "ACSP,ACCEPTED", "ACTC,ACCEPTED", "ACWC,ACCEPTED",
            "ACWP,ACCEPTED", "RJCT,REJECTED", "CANC,INITIATED", "PART,INITIATED", "PDNG,INITIATED"})
    void shouldMapToSepaPaymentStatus(String inputPaymentInformationStatusCode, String expectedSepaPaymentStatus) {
        // given
        StetPaymentStatus statusCode = StetPaymentStatus.valueOf(inputPaymentInformationStatusCode);
        SepaPaymentStatus paymentStatus = SepaPaymentStatus.valueOf(expectedSepaPaymentStatus);

        // when
        SepaPaymentStatus sepaPaymentStatus = paymentMapper.mapToSepaPaymentStatus(statusCode);

        // then
        assertThat(sepaPaymentStatus).isEqualTo(paymentStatus);
    }

    private InitiatePaymentRequest createInitiatePaymentRequest(String baseClientRedirectUrl) {
        return new InitiatePaymentRequestBuilder()
                .setBaseClientRedirectUrl(baseClientRedirectUrl)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setState(STATE)
                .setRequestDTO(createSepaInitiatePaymentRequestDTO())
                .build();
    }

    private SepaInitiatePaymentRequestDTO createSepaInitiatePaymentRequestDTO() {
        return SepaInitiatePaymentRequestDTO.builder()
                .remittanceInformationUnstructured("RemittanceInformationUnstructured")
                .creditorName("CreditorName")
                .creditorAccount(createSepaAccountDTO("FR9017569000503789157177S63"))
                .debtorAccount(createSepaAccountDTO("FR0530003000402558963954F37"))
                .executionDate(LocalDate.now())
                .endToEndIdentification("EndToEndIdentification")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("10.50"))
                        .build())
                .instructionPriority(InstructionPriority.NORMAL)
                .build();
    }

    private SepaAccountDTO createSepaAccountDTO(String iban) {
        return SepaAccountDTO.builder()
                .iban(iban)
                .currency(EUR)
                .build();
    }

    @SneakyThrows
    private DefaultAuthenticationMeans createDefaultAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .clientId("clientId")
                .clientName("clientName")
                .clientEmail("client@yolt.com")
                .clientSigningKeyId(UUID.randomUUID())
                .clientSigningCertificate(KeyUtil.createCertificateFromPemFormat(readCertificatePem()))
                .clientTransportKeyId(UUID.randomUUID())
                .clientTransportCertificate(KeyUtil.createCertificateFromPemFormat(readCertificatePem()))
                .clientWebsiteUri("https://yolt.com/")
                .build();
    }

    @SneakyThrows
    private String readCertificatePem() {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }

    @SneakyThrows
    private Consumer<StetPaymentInitiationRequestDTO> validatePaymentRequestResource(InitiatePaymentRequest request) {
        String urlEncodedRedirectUrlWithState = URLEncoder.encode(REDIRECT_URL_WITH_STATE, StandardCharsets.UTF_8);
        String urlEncodedRedirectUrlWithStateAndError = URLEncoder.encode(REDIRECT_URL_WITH_STATE_AND_ERROR, StandardCharsets.UTF_8);

        return (paymentInitiationRequestDTO) -> {
            assertThat(paymentInitiationRequestDTO.getChargeBearer()).isEqualTo(StetChargeBearer.SLEV);
            assertThat(paymentInitiationRequestDTO.getCreationDateTime()).isNotNull();
            assertThat(paymentInitiationRequestDTO.getNumberOfTransactions()).isEqualTo(1);
            assertThat(paymentInitiationRequestDTO.getPaymentInformationId()).matches("[a-f0-9]{32}");

            StetSupplementaryDataDTO supplementaryData = (StetSupplementaryDataDTO) paymentInitiationRequestDTO.getSupplementaryData();
            assertThat(supplementaryData.getAppliedAuthenticationApproach()).isEqualTo(StetAuthenticationApproach.REDIRECT);
            assertThat(supplementaryData.getSuccessfulReportUrl()).isEqualTo(urlEncodedRedirectUrlWithState);
            assertThat(supplementaryData.getUnsuccessfulReportUrl()).isEqualTo(urlEncodedRedirectUrlWithStateAndError);

            StetPaymentTypeInformationDTO paymentTypeInformation = paymentInitiationRequestDTO.getPaymentTypeInformation();
            assertThat(paymentTypeInformation.getCategoryPurpose()).isEqualTo(StetCategoryPurpose.CASH);
            assertThat(paymentTypeInformation.getServiceLevel()).isEqualTo(StetServiceLevel.SEPA);

            validateCreditTransferTransactionResources(request.getRequestDTO())
                    .accept(paymentInitiationRequestDTO.getCreditTransferTransaction());
        };
    }

    private Consumer<List<StetCreditTransferTransaction>> validateCreditTransferTransactionResources(SepaInitiatePaymentRequestDTO requestDTO) {
        return (creditTransferTransactions) -> {
            assertThat(creditTransferTransactions).hasSize(1);

            StetCreditTransferTransaction creditTransferTransaction = creditTransferTransactions.get(0);
            assertThat(creditTransferTransaction.getRequestedExecutionDate()).isNotNull();

            if (creditTransferTransaction instanceof StetCreditTransferTransactionDTO) {
                StetCreditTransferTransactionDTO creditTransferTransactionDTO = (StetCreditTransferTransactionDTO) creditTransferTransaction;
                StetRemittanceInformationDTO remittanceInformation = creditTransferTransactionDTO.getRemittanceInformation();
                assertThat(remittanceInformation.getUnstructured()).containsExactly("RemittanceInformationUnstructured");
            } else {
                StetCreditTransferTransactionNoUnstructured creditTransferTransactionNoUnstructured =
                        (StetCreditTransferTransactionNoUnstructured) creditTransferTransaction;
                assertThat(creditTransferTransactionNoUnstructured.getRemittanceInformation()).containsExactly("RemittanceInformationUnstructured");
            }

            StetAmountTypeDTO instructedAmount = creditTransferTransaction.getInstructedAmount();
            assertThat(instructedAmount.getAmount()).isEqualTo(10.50F);
            assertThat(instructedAmount.getCurrency()).isEqualTo(EUR.name());

            StetPaymentIdentificationDTO paymentId = creditTransferTransaction.getPaymentId();
            assertThat(paymentId.getEndToEndId()).isEqualTo("EndToEndIdentification");
            assertThat(paymentId.getInstructionId()).matches("[a-f0-9]{32}");
        };
    }
}
