package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "Document")
@XmlAccessorType(XmlAccessType.FIELD)
public class Document {
    @XmlElement(name = "BkToCstmrAcctRpt")
    private BankToCustomerAccountReport bankToCustomerAccountReport;
}
