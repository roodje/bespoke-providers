package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "NtryDtls")
@XmlAccessorType(XmlAccessType.FIELD)
public class EntryDetails {
    @XmlElement(name = "TxDtls")
    private TransactionDetails transactionDetails;
}
