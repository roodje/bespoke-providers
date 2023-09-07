package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.ing.common.dto.CreditorAccount;
import com.yolt.providers.ing.common.dto.DebtorAccount;
import com.yolt.providers.ing.common.dto.InstructedAmount;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

public class DefaultInitiatePaymentHttpRequestBodyMapper {
    public InstructedAmount toInstructedAmount(final SepaAmountDTO sepaAmountDTO) {
        return new InstructedAmount(
                sepaAmountDTO.getAmount().toPlainString(),
                CurrencyCode.EUR.name()
        );
    }

    public DebtorAccount toDebtorAccount(final SepaAccountDTO sepaAccountDTO) {
        return new DebtorAccount(
                sepaAccountDTO.getCurrency().name(),
                sepaAccountDTO.getIban()
        );
    }

    public CreditorAccount toCreditorAccount(final SepaAccountDTO sepaAccountDTO) {
        return new CreditorAccount(
                sepaAccountDTO.getCurrency().name(),
                sepaAccountDTO.getIban()
        );
    }
}
