package com.yolt.providers.cbiglobe.common.model;

public enum TransactionStatus {

    /**
     * AcceptedCustomerProfile - Preceding check of technical validation was successful. Customer profile check was also successful.
     **/
    ACCP,

    /**
     * AcceptedSettlementCompleted - Settlement on the debtor's account has been completed. Usage : this can be used by the first agent
     * to report to the debtor that the transaction has been completed. Warning : this status is provided for transaction status reasons,
     * not for financial information. It can only be used after bilateral agreement.
     **/
    ACSC,

    /**
     * AcceptedSettlementInProcess - All preceding checks such as technical validation and customer profile were successful and therefore the
     * payment initiation has been accepted for execution.
     **/
    ACSP,

    /**
     * AcceptedTechnicalValidation - Authentication and syntactical and semantical validation are successful.
     **/
    ACTC,

    /**
     * AcceptedWithChange - Instruction is accepted but a change will be made, such as date or remittance not sent.
     **/
    ACWC,

    /**
     * AcceptedWithoutPosting - Payment instruction included in the credit transfer is accepted without being posted to the creditor customer's account.
     **/
    ACWP,

    /**
     * Received - Payment initiation has been received by the receiving agent.
     **/
    RCVD,

    /**
     * Pending - Payment initiation or individual transaction included in the payment initiation is pending. Further checks and status update will be performed.
     **/
    PDNG,

    /**
     * Rejected - Payment initiation or individual transaction included in the payment initiation has been rejected.
     **/
    RJCT,

    /**
     * Debtor Account Selection Initiated - Payment initiation or individual transaction included in the payment initiation is missed for debtorAccount and ASPSP has been invoked to start the Account Selection steps.
     **/
    DAS_I,

    /**
     * Debtor Account Selection Failed - Payment initiation or individual transaction included in the payment initiation is missed for debtorAccount but something went wrong during initialization or finalization of this processing phase
     **/
    DAS_FAILED,

    /**
     * SCA Selection required for Debtor Account Selection - Payment initiation or individual transaction included in the payment initiation is missed for debtorAccount and PSU must choose one of the available SCA methods to move forward the request processing.
     **/
    DAS_SR,

    /**
     * PSU Credentials required for Debtor Account Selection - Payment initiation or individual transaction included in the payment initiation is missed for debtorAccount and PSU must provide its credential to move forward the request processing.
     **/
    DAS_CR
}
