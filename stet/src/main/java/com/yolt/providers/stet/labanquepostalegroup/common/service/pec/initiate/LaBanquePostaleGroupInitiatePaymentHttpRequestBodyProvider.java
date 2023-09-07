package com.yolt.providers.stet.labanquepostalegroup.common.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.DynamicFields;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class LaBanquePostaleGroupInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<StetInitiatePreExecutionResult, StetPaymentInitiationRequestDTO> {

    private static final float MINIMAL_AMOUNT = 1.5f;
    private final DateTimeSupplier dateTimeSupplier;

    @SneakyThrows
    @Override
    public StetPaymentInitiationRequestDTO provideHttpRequestBody(StetInitiatePreExecutionResult preExecutionResult) {
        SepaInitiatePaymentRequestDTO requestDTO = preExecutionResult.getSepaRequestDTO();
        OffsetDateTime currentDateTime = dateTimeSupplier.getDefaultOffsetDateTime();

        StetCreditTransferTransactionDTO creditTransferTransactionDTO = StetCreditTransferTransactionDTO.builder()
                .requestedExecutionDate(mapToRequestedExecutionDate(requestDTO, currentDateTime))
                .remittanceInformation(mapToRemittanceInformation(requestDTO))
                .instructedAmount(mapToInstructedAmount(requestDTO))
                .paymentId(mapToPaymentIdentification(requestDTO))
                .beneficiary(mapToBeneficiary(requestDTO))
                .build();

        return StetPaymentInitiationRequestDTO.builder()
                .paymentInformationId(createUniqueId())
                .beneficiary(mapToBeneficiary(requestDTO))
                .numberOfTransactions(1)
                .initiatingParty(mapToInitiatingParty(requestDTO))
                .paymentTypeInformation(createPaymentTypeInformation())
                .creationDateTime(mapToCreationDateTime(requestDTO, currentDateTime))
                .debtorAccount(mapToAccountIdentification(requestDTO.getDebtorAccount()))
                .chargeBearer(StetChargeBearer.SLEV)
                .supplementaryData(mapToSupplementaryData(preExecutionResult))
                .requestedExecutionDate(mapToRequestedExecutionDate(requestDTO, currentDateTime))
                .creditTransferTransaction(Collections.singletonList(creditTransferTransactionDTO))
                .build();
    }

    private StetPartyIdentificationDTO mapToInitiatingParty(SepaInitiatePaymentRequestDTO requestDTO) {
        DynamicFields dynamicFields = requestDTO.getDynamicFields();
        if (dynamicFields != null) {
            return StetPartyIdentificationDTO.builder()
                    .name(Objects.requireNonNull(requestDTO.getDynamicFields()).getCreditorAgentName())
                    .build();
        } else return null;
    }

    private StetPaymentTypeInformationDTO createPaymentTypeInformation() {
        return StetPaymentTypeInformationDTO.builder()
                .serviceLevel(StetServiceLevel.SEPA)
                .categoryPurpose(StetCategoryPurpose.CASH)
                .build();
    }

    private StetPaymentBeneficiaryDTO mapToBeneficiary(SepaInitiatePaymentRequestDTO request) {

        StetPaymentBeneficiaryDTO beneficiary = StetPaymentBeneficiaryDTO.builder()
                .creditor(mapToPartyIdentification(request.getCreditorName()))
                .creditorAccount(mapToAccountIdentification(request.getCreditorAccount()))
                .build();

        DynamicFields dynamicFields = request.getDynamicFields();
        if (dynamicFields != null) {
            beneficiary.setCreditorAgent(StetFinancialInstitutionIdentificationDTO.builder()
                    .bicFi(dynamicFields.getCreditorAgentBic())
                    .name(dynamicFields.getCreditorAgentName())
                    .build());
        }
        return beneficiary;
    }

    private StetPartyIdentificationDTO mapToPartyIdentification(String name) {
        return StetPartyIdentificationDTO.builder()
                .name(name)
                .build();
    }

    private OffsetDateTime mapToRequestedExecutionDate(SepaInitiatePaymentRequestDTO requestDTO, OffsetDateTime currentDateTime) {
        return dateTimeSupplier.convertOrGetDefaultOffsetDateTime(requestDTO.getExecutionDate(), currentDateTime);
    }


    private OffsetDateTime mapToCreationDateTime(SepaInitiatePaymentRequestDTO requestDTO, OffsetDateTime currentDateTime) {
        return dateTimeSupplier.convertOrGetDefaultOffsetDateTime(requestDTO.getExecutionDate(), currentDateTime);
    }

    private StetAmountTypeDTO mapToInstructedAmount(SepaInitiatePaymentRequestDTO request) {
        float extractedAmount = request.getInstructedAmount().getAmount().floatValue();
        if (extractedAmount < MINIMAL_AMOUNT) {
            throw new IllegalArgumentException("Amount must be at least 1,5 EUR");
        }
        return StetAmountTypeDTO.builder()
                .amount(extractedAmount)
                .currency(CurrencyCode.EUR.name())
                .build();
    }

    private String createUniqueId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private StetAccountIdentificationDTO mapToAccountIdentification(SepaAccountDTO accountDTO) {
        if (!ObjectUtils.isEmpty(accountDTO)) {
            return StetAccountIdentificationDTO.builder()
                    .iban(accountDTO.getIban())
                    .build();
        }
        return null;
    }

    @SneakyThrows
    private StetSupplementaryDataDTO mapToSupplementaryData(StetInitiatePreExecutionResult preExecutionResult) {
        String redirectUrl = preExecutionResult.getBaseClientRedirectUrl();
        if (StringUtils.isEmpty(redirectUrl)) {
            throw new IllegalStateException("Unable to create PaymentRequestResource due to missing base client redirect URL");
        }
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUrl);
        return StetSupplementaryDataDTO.builder()
                .appliedAuthenticationApproach(StetAuthenticationApproach.REDIRECT)
                .successfulReportUrl(URLEncoder.encode(uriBuilder.queryParam("state", preExecutionResult.getState()).toUriString(), "UTF-8"))
                .unsuccessfulReportUrl(URLEncoder.encode(uriBuilder.queryParam("error", "wrong").toUriString(), "UTF-8"))
                .build();
    }

    private StetRemittanceInformationDTO mapToRemittanceInformation(SepaInitiatePaymentRequestDTO requestDTO) {
        return StetRemittanceInformationDTO.builder()
                .unstructured(Collections.singletonList(requestDTO.getRemittanceInformationUnstructured()))
                .build();
    }

    private StetPaymentIdentificationDTO mapToPaymentIdentification(SepaInitiatePaymentRequestDTO request) {
        return StetPaymentIdentificationDTO.builder()
                .instructionId(createUniqueId())
                .endToEndId(request.getEndToEndIdentification())
                .build();
    }
}
