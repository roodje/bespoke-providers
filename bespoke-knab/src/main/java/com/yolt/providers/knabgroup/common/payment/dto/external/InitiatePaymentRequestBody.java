package com.yolt.providers.knabgroup.common.payment.dto.external;

import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

@RequiredArgsConstructor
@Getter
public class InitiatePaymentRequestBody {

    private final BankAccount creditorAccount;
    private final BankAccount debtorAccount;
    private final InstructedAmount instructedAmount;
    private final String creditorName;
    private final String remittanceInformationUnstructured;

    @RequiredArgsConstructor
    @Getter
    public static class BankAccount {
        private final String iban;

        public BankAccount(SepaAccountDTO sepaAccountDTO) {
            this.iban = sepaAccountDTO.getIban();
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class InstructedAmount {

        private final String amount;
        private final String currency;

        public InstructedAmount(SepaAmountDTO sepaAmountDTO) {
            this.amount = sepaAmountDTO.getAmount().toPlainString();
            this.currency = CurrencyCode.EUR.name();
        }
    }
}
