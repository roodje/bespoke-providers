package com.yolt.providers.stet.generic.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * List of all payment statuses based on the STET documentation.
 * Link: https://www.stet.eu/assets/files/PSD2/1-4-2/api-dsp2-stet-v1.4.2.17-part-2-functional-model.pdf
 * Section: 4.1.6. PaymentInformationStatusCode, Page: 13
 */
@Getter
@AllArgsConstructor
public enum StetPaymentStatus {

  /**
   * (AcceptedCustomerProfile):
   * Preceding check of technical validation was successful.
   * Customer profile check was also successful.
   */
  ACCP("ACCP"),

  /**
   * (AcceptedSettlementCompleted):
   * Settlement on the debtor's account has been completed.
   */
  ACSC("ACSC"),

  /**
   * (AcceptedSettlementInProcess):
   * All preceding checks such as technical validation and customer profile were successful.
   * Dynamic risk assessment is now also successful and therefore the Payment Request has been accepted for execution.
   */
  ACSP("ACSP"),

  /**
   * (AcceptedTechnicalValidation):
   * Authentication and syntactical and semantical validation are successful.
   */
  ACTC("ACTC"),

  /**
   * (AcceptedWithChange):
   * Instruction is accepted but a change will be made, such as date or remittance not sent.
   */
  ACWC("ACWC"),

  /**
   * (AcceptedWithoutPosting):
   * Payment instruction included in the credit transfer is accepted without being posted to the creditor customer’s account.
   */
  ACWP("ACWP"),

  /**
   * (PartiallyAccepted):
   * A number of transactions have been accepted, whereas another number of transactions have not yet achieved 'accepted' status.
   */
  PART("PART"),

  /**
   * (PartiallyAcceptedTechnicalCorrect):
   * Payment initiation needs multiple authentications, where some but not yet all have been performed.
   * Syntactical and semantical validations are successful.
   */
  PATC("PATC"),

  /**
   * (Received):
   * Payment initiation has been received by the receiving agent.
   */
  RCVD("RCVD"),

  /**
   * (Pending):
   * Payment request or individual transaction included in the Payment Request is pending.
   * Further checks and status update will be performed.
   */
  PDNG("PDNG"),

  /**
   * (Cancelled):
   * Payment initiation has been successfully cancelled after having received a request for cancellation.
   */
  CANC("CANC"),

  /**
   * (Rejected):
   * Payment request has been rejected.
   */
  RJCT("RJCT");

  private String value;
}