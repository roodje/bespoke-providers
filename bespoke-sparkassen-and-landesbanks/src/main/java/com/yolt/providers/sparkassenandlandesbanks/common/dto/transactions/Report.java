package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "Rpt")
@XmlAccessorType(XmlAccessType.FIELD)
public class Report {
    @XmlElement(name = "Ntry")
    private TransactionEntry[] transactionEntries;
}
