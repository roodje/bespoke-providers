package com.yolt.providers.stet.lclgroup.common.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class LclGroupInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<StetInitiatePreExecutionResult, StetPaymentInitiationRequestDTO> {

    private final Clock clock;
    private final DateTimeSupplier dateTimeSupplier;

    public StetPaymentInitiationRequestDTO provideHttpRequestBody(StetInitiatePreExecutionResult preExecutionResult) {
        OffsetDateTime currentDateTime = OffsetDateTime.now(clock);
        SepaInitiatePaymentRequestDTO requestDTO = preExecutionResult.getSepaRequestDTO();

        StetCreditTransferTransactionDTO creditTransferTransaction = mapToCreditTransferTransaction(requestDTO, currentDateTime);

        return StetPaymentInitiationRequestDTO.builder()
                .paymentInformationId(createUniqueId())
                .beneficiary(mapToBeneficiary(requestDTO))
                .numberOfTransactions(1)
                .initiatingParty(mapToInitiatingParty(requestDTO, preExecutionResult.getAuthMeans()))
                .paymentTypeInformation(createPaymentTypeInformation())
                .creationDateTime(mapToCreationDateTime(requestDTO, currentDateTime))
                .debtorAccount(mapToAccountIdentification(requestDTO.getDebtorAccount()))
                .chargeBearer(StetChargeBearer.SLEV)
                .supplementaryData(mapToSupplementaryArrayData(preExecutionResult))
                .creditTransferTransaction(Collections.singletonList(creditTransferTransaction))
                .build();
    }

    private OffsetDateTime mapToRequestedExecutionDate(SepaInitiatePaymentRequestDTO requestDTO, OffsetDateTime currentDateTime) {
        return dateTimeSupplier.convertOrGetDefaultOffsetDateTime(requestDTO.getExecutionDate(), currentDateTime.plusSeconds(30));
    }

    private StetRemittanceInformationDTO mapToRemittanceInformation(SepaInitiatePaymentRequestDTO requestDTO) {
        return StetRemittanceInformationDTO.builder()
                .unstructured(Collections.singletonList(requestDTO.getRemittanceInformationUnstructured()))
                .build();
    }

    private StetAmountTypeDTO mapToInstructedAmount(SepaInitiatePaymentRequestDTO request) {
        return StetAmountTypeDTO.builder()
                .amount(request.getInstructedAmount().getAmount().floatValue())
                .currency(CurrencyCode.EUR.name())
                .build();
    }

    private StetPaymentIdentificationDTO mapToPaymentIdentification(SepaInitiatePaymentRequestDTO request) {
        return StetPaymentIdentificationDTO.builder()
                .instructionId(createUniqueId())
                .endToEndId(request.getEndToEndIdentification())
                .build();
    }

    private StetPaymentBeneficiaryDTO mapToBeneficiary(SepaInitiatePaymentRequestDTO request) {
        return StetPaymentBeneficiaryDTO.builder()
                .creditor(mapToPartyIdentification(request.getCreditorName()))
                .creditorAccount(mapToAccountIdentification(request.getCreditorAccount()))
                .build();
    }

    private StetCreditTransferTransactionDTO mapToCreditTransferTransaction(SepaInitiatePaymentRequestDTO request,
                                                                            OffsetDateTime currentDateTime) {
        return StetCreditTransferTransactionDTO.builder()
                .remittanceInformation(mapToRemittanceInformation(request))
                .instructedAmount(mapToInstructedAmount(request))
                .paymentId(mapToPaymentIdentification(request))
                .requestedExecutionDate(mapToRequestedExecutionDate(request, currentDateTime))
                .build();
    }

    private String createUniqueId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private StetPartyIdentificationDTO mapToInitiatingParty(SepaInitiatePaymentRequestDTO requestDTO,
                                                            DefaultAuthenticationMeans authMeans) { //NOSONAR It allows others to use it
        return mapToPartyIdentification(requestDTO.getCreditorName());
    }

    private StetPartyIdentificationDTO mapToPartyIdentification(String name) {
        return StetPartyIdentificationDTO.builder()
                .name(name)
                .build();
    }

    private StetPaymentTypeInformationDTO createPaymentTypeInformation() {
        return StetPaymentTypeInformationDTO.builder()
                .categoryPurpose(StetCategoryPurpose.CASH)
                .instructionPriority(StetPriorityCode.NORM)
                .serviceLevel(StetServiceLevel.SEPA)
                .build();
    }

    private OffsetDateTime mapToCreationDateTime(SepaInitiatePaymentRequestDTO requestDTO, //NOSONAR It allows others to use it
                                                 OffsetDateTime currentDateTime) {
        return currentDateTime;
    }

    private StetSupplementaryDataArrayDTO mapToSupplementaryArrayData(StetInitiatePreExecutionResult preExecutionResult) {
        String redirectUrl = preExecutionResult.getBaseClientRedirectUrl();
        if (StringUtils.isEmpty(redirectUrl)) {
            throw new IllegalStateException("Unable to create PaymentRequestResource due to missing base client redirect URL");
        }
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUrl);
        return StetSupplementaryDataArrayDTO.builder()
                .acceptedAuthenticationApproach(List.of(StetAuthenticationApproach.REDIRECT))
                .successfulReportUrl(uriBuilder.queryParam("state", preExecutionResult.getState()).toUriString())
                .unsuccessfulReportUrl(uriBuilder.toUriString())
                .build();
    }

    private StetAccountIdentificationDTO mapToAccountIdentification(SepaAccountDTO accountDTO) {
        if (!ObjectUtils.isEmpty(accountDTO)) {
            return StetAccountIdentificationDTO.builder()
                    .iban(accountDTO.getIban())
                    .build();
        }
        return null;
    }
}
