package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "BkToCstmrAcctRpt")
@XmlAccessorType(XmlAccessType.FIELD)
public class BankToCustomerAccountReport {
    @XmlElement(name = "GrpHdr")
    private GroupHeader groupHeader;
    @XmlElement(name = "Rpt")
    private Report report;
}
