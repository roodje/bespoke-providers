package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "TxDtls")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransactionDetails {
    @XmlElement(name = "Refs")
    private References references;
    @XmlElement(name = "RltdPties")
    private RelatedParties relatedParties;
    @XmlElement(name = "RmtInf")
    private RemittanceInformation remittanceInformation;
}
