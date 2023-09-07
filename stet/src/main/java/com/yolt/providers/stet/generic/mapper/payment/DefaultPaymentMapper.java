package com.yolt.providers.stet.generic.mapper.payment;

import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@RequiredArgsConstructor
public class DefaultPaymentMapper implements PaymentMapper {

    protected final DateTimeSupplier dateTimeSupplier;

    @Override
    public StetPaymentInitiationRequestDTO mapToStetPaymentInitiationRequestDTO(InitiatePaymentRequest request,
                                                                                DefaultAuthenticationMeans authMeans) {
        OffsetDateTime currentDateTime = dateTimeSupplier.getDefaultOffsetDateTime();
        SepaInitiatePaymentRequestDTO requestDTO = request.getRequestDTO();

        StetCreditTransferTransaction creditTransferTransaction = StetCreditTransferTransactionDTO.builder()
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
                .initiatingParty(mapToInitiatingParty(requestDTO, authMeans))
                .paymentTypeInformation(createPaymentTypeInformation())
                .creationDateTime(mapToCreationDateTime(requestDTO, currentDateTime))
                .debtorAccount(mapToAccountIdentification(requestDTO.getDebtorAccount()))
                .chargeBearer(StetChargeBearer.SLEV)
                .supplementaryData(mapToSupplementaryData(request))
                .requestedExecutionDate(mapToRequestedExecutionDate(requestDTO, currentDateTime))
                .creditTransferTransaction(Collections.singletonList(creditTransferTransaction))
                .build();
    }

    protected OffsetDateTime mapToRequestedExecutionDate(SepaInitiatePaymentRequestDTO requestDTO, OffsetDateTime currentDateTime) {
        return dateTimeSupplier.convertOrGetDefaultOffsetDateTime(requestDTO.getExecutionDate(), currentDateTime.plusSeconds(30));
    }

    protected StetRemittanceInformationDTO mapToRemittanceInformation(SepaInitiatePaymentRequestDTO requestDTO) {
        return StetRemittanceInformationDTO.builder()
                .unstructured(Collections.singletonList(requestDTO.getRemittanceInformationUnstructured()))
                .build();
    }

    protected StetAmountTypeDTO mapToInstructedAmount(SepaInitiatePaymentRequestDTO request) {
        return StetAmountTypeDTO.builder()
                .amount(request.getInstructedAmount().getAmount().floatValue())
                .currency(CurrencyCode.EUR.name())
                .build();
    }

    protected StetPaymentIdentificationDTO mapToPaymentIdentification(SepaInitiatePaymentRequestDTO request) {
        return StetPaymentIdentificationDTO.builder()
                .instructionId(createUniqueId())
                .endToEndId(request.getEndToEndIdentification())
                .build();
    }

    protected StetPaymentBeneficiaryDTO mapToBeneficiary(SepaInitiatePaymentRequestDTO request) {
        return StetPaymentBeneficiaryDTO.builder()
                .creditor(mapToPartyIdentification(request.getCreditorName()))
                .creditorAccount(mapToAccountIdentification(request.getCreditorAccount()))
                .build();
    }

    protected String createUniqueId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    protected StetPartyIdentificationDTO mapToInitiatingParty(SepaInitiatePaymentRequestDTO requestDTO,
                                                              DefaultAuthenticationMeans authMeans) { //NOSONAR It allows others to use it
        return mapToPartyIdentification(requestDTO.getCreditorName());
    }

    protected StetPartyIdentificationDTO mapToPartyIdentification(String name) {
        return StetPartyIdentificationDTO.builder()
                .name(name)
                .build();
    }

    protected StetPaymentTypeInformationDTO createPaymentTypeInformation() {
        return StetPaymentTypeInformationDTO.builder()
                .serviceLevel(StetServiceLevel.SEPA)
                .categoryPurpose(StetCategoryPurpose.CASH)
                .build();
    }

    protected OffsetDateTime mapToCreationDateTime(SepaInitiatePaymentRequestDTO requestDTO, //NOSONAR It allows others to use it
                                                   OffsetDateTime currentDateTime) {
        return currentDateTime;
    }

    @SneakyThrows
    protected StetSupplementaryDataDTO mapToSupplementaryData(InitiatePaymentRequest request) {
        String redirectUrl = request.getBaseClientRedirectUrl();
        if (StringUtils.isEmpty(redirectUrl)) {
            throw new IllegalStateException("Unable to create PaymentRequestResource due to missing base client redirect URL");
        }
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUrl);
        return StetSupplementaryDataDTO.builder()
                .appliedAuthenticationApproach(StetAuthenticationApproach.REDIRECT)
                .successfulReportUrl(URLEncoder.encode(uriBuilder.queryParam("state", request.getState()).toUriString(), "UTF-8"))
                .unsuccessfulReportUrl(URLEncoder.encode(uriBuilder.queryParam("error", "wrong").toUriString(), "UTF-8"))
                .build();
    }

    protected StetAccountIdentificationDTO mapToAccountIdentification(SepaAccountDTO accountDTO) {
        if (accountDTO != null) {
            CurrencyCode currency = accountDTO.getCurrency();
            return StetAccountIdentificationDTO.builder()
                    .iban(accountDTO.getIban())
                    .currency(Objects.nonNull(currency) ? currency.name() : null)
                    .build();
        }
        return null;
    }

    @Override
    public SepaPaymentStatus mapToSepaPaymentStatus(StetPaymentStatus stetPaymentStatus) {
        switch (stetPaymentStatus) {
            case ACCP:
            case ACSC:
            case ACSP:
            case ACTC:
            case ACWC:
            case ACWP:
                return SepaPaymentStatus.ACCEPTED;
            case RJCT:
                return SepaPaymentStatus.REJECTED;
            default:
                return SepaPaymentStatus.INITIATED;
        }
    }
}
