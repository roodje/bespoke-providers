package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "RltdPties")
@XmlAccessorType(XmlAccessType.FIELD)
public class RelatedParties {
    @XmlElement(name = "DbtrAcct")
    private DebtorAccount debtorAccount;
    @XmlElement(name = "CdtrAcct")
    private CreditorAccount creditorAccount;
    @XmlElement(name = "Cdtr")
    private Creditor creditor;
    @XmlElement(name = "Dbtr")
    private Debtor debtor;
}
