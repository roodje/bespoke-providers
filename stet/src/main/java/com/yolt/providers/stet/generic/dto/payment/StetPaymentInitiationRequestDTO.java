package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class StetPaymentInitiationRequestDTO {

    private String resourceId;
    private String paymentInformationId;
    private StetPaymentBeneficiaryDTO beneficiary;
    private Boolean batchBooking;
    private OffsetDateTime creationDateTime;
    private Integer numberOfTransactions;
    private StetPartyIdentificationDTO initiatingParty;
    private Boolean acceptDebtorAccountChange;
    private Boolean acceptChargeHandlingChange;
    private Boolean acceptInstantPaymentDowngrade;
    private StetPaymentTypeInformationDTO paymentTypeInformation;
    private StetPartyIdentificationDTO debtor;
    private StetAccountIdentificationDTO debtorAccount;
    private StetSupplementaryData supplementaryData;
    private StetChargeBearer chargeBearer;
    private StetPaymentStatus paymentInformationStatus;
    private Boolean fundsAvailability;
    private Boolean booking;
    private OffsetDateTime requestedExecutionDate;
    private List<StetCreditTransferTransaction> creditTransferTransaction;
}
