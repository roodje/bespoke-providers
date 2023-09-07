package com.yolt.providers.stet.labanquepostalegroup.common.mapper.payment;

import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.sepa.DynamicFields;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.payment.DefaultPaymentMapper;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.auth.LaBanquePostaleAuthenticationMeans;
import com.yolt.securityutils.certificate.CertificateParser;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.time.OffsetDateTime;
import java.util.Collections;

public class LaBanquePostaleGroupPaymentMapper extends DefaultPaymentMapper {

    private static final float MINIMAL_AMOUNT = 1.5f;

    public LaBanquePostaleGroupPaymentMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    public StetPaymentInitiationRequestDTO mapToStetPaymentInitiationRequestDTO(InitiatePaymentRequest request,
                                                                                DefaultAuthenticationMeans authMeans) {
        OffsetDateTime currentDateTime = dateTimeSupplier.getDefaultOffsetDateTime();
        SepaInitiatePaymentRequestDTO requestDTO = request.getRequestDTO();

        StetCreditTransferTransactionDTO creditTransferTransaction = StetCreditTransferTransactionDTO.builder()
                .requestedExecutionDate(mapToRequestedExecutionDate(requestDTO, currentDateTime))
                .remittanceInformation(mapToRemittanceInformation(requestDTO))
                .instructedAmount(mapToInstructedAmount(requestDTO))
                .paymentId(mapToPaymentIdentification(requestDTO))
                .build();

        return StetPaymentInitiationRequestDTO.builder()
                .paymentInformationId(createUniqueId())
                .numberOfTransactions(1)
                .initiatingParty(mapToInitiatingParty(requestDTO, authMeans))
                .paymentTypeInformation(createPaymentTypeInformation())
                .creationDateTime(mapToCreationDateTime(requestDTO, currentDateTime))
                .chargeBearer(StetChargeBearer.SLEV)
                .supplementaryData(mapToSupplementaryData(request))
                .requestedExecutionDate(mapToRequestedExecutionDate(requestDTO, currentDateTime))
                .creditTransferTransaction(Collections.singletonList(creditTransferTransaction))
                .build();
    }

    @Override
    protected StetPartyIdentificationDTO mapToInitiatingParty(SepaInitiatePaymentRequestDTO requestDTO,
                                                              DefaultAuthenticationMeans authMeans) {

        return mapToPartyIdentification(CertificateParser.getOrganization(((LaBanquePostaleAuthenticationMeans) authMeans).getClientTransportCertificateChain()[0]));
    }

    @Override
    protected OffsetDateTime mapToRequestedExecutionDate(SepaInitiatePaymentRequestDTO requestDTO, OffsetDateTime currentDateTime) {
        return dateTimeSupplier.convertOrGetDefaultOffsetDateTime(requestDTO.getExecutionDate(), currentDateTime);
    }

    @Override
    protected OffsetDateTime mapToCreationDateTime(SepaInitiatePaymentRequestDTO requestDTO, OffsetDateTime currentDateTime) {
        return dateTimeSupplier.convertOrGetDefaultOffsetDateTime(requestDTO.getExecutionDate(), currentDateTime);
    }

    @Override
    @SneakyThrows
    protected StetAmountTypeDTO mapToInstructedAmount(SepaInitiatePaymentRequestDTO request) {
        float extractedAmount = request.getInstructedAmount().getAmount().floatValue();

        if (extractedAmount < MINIMAL_AMOUNT) {
            throw new CreationFailedException("Amount must be at least 1,5 EUR");
        }
        return StetAmountTypeDTO.builder()
                .amount(extractedAmount)
                .currency(CurrencyCode.EUR.name())
                .build();
    }
}