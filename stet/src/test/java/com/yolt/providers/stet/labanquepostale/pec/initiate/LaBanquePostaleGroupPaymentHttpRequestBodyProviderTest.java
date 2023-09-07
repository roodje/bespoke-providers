package com.yolt.providers.stet.labanquepostale.pec.initiate;

import com.yolt.providers.common.pis.sepa.InstructionPriority;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import com.yolt.providers.stet.labanquepostalegroup.common.service.pec.initiate.LaBanquePostaleGroupInitiatePaymentHttpRequestBodyProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

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

public class LaBanquePostaleGroupPaymentHttpRequestBodyProviderTest {
    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
    private static final String STATE = "174d3210-0497-4181-b2fb-c1798a2b777f";
    private static final String PSU_IP_ADDRESS = "128.0.0.1";
    private static final String BASE_CLIENT_REDIRECT_URL = "https://yolt.com/payment";
    private static final String REDIRECT_URL_WITH_STATE = String.format("%s?state=%s", BASE_CLIENT_REDIRECT_URL, STATE);
    private static final String REDIRECT_URL_WITH_STATE_AND_ERROR = String.format("%s&error=wrong", REDIRECT_URL_WITH_STATE);

    private LaBanquePostaleGroupInitiatePaymentHttpRequestBodyProvider initiatePaymentHttpRequestBodyProvider =
            new LaBanquePostaleGroupInitiatePaymentHttpRequestBodyProvider(new DateTimeSupplier(Clock.systemUTC()));

    @Test
    void shouldProvideRequestBody() {
        // given
        StetInitiatePreExecutionResult preExecutionResult = StetInitiatePreExecutionResult.builder()
                .authMeans(createDefaultAuthenticationMeans())
                .baseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .psuIpAddress(PSU_IP_ADDRESS)
                .state(STATE)
                .sepaRequestDTO(createSepaInitiatePaymentRequestDTO())
                .build();

        // when
        StetPaymentInitiationRequestDTO paymentInitiationRequestDTO = initiatePaymentHttpRequestBodyProvider.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(paymentInitiationRequestDTO).satisfies(validatePaymentRequestResource(preExecutionResult));
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
    private Consumer<StetPaymentInitiationRequestDTO> validatePaymentRequestResource(StetInitiatePreExecutionResult preExecutionResult) {
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

            validateCreditTransferTransactionResources(preExecutionResult.getSepaRequestDTO())
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

            StetPaymentBeneficiaryDTO beneficiary = creditTransferTransaction.getBeneficiary();
            assertThat(beneficiary.getCreditor().getName()).isEqualTo(requestDTO.getCreditorName());
            assertThat(beneficiary.getCreditorAccount().getIban()).isEqualTo(requestDTO.getCreditorAccount().getIban());
        };
    }
}
