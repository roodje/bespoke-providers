package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "Ntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransactionEntry {
    @XmlElement(name = "Amt")
    private Amount amount;
    @XmlElement(name = "CdtDbtInd")
    private String creditDebitIndicator;
    @XmlElement(name = "Sts")
    private String status;
    @XmlElement(name = "BookgDt")
    private BookingDate bookingDate;
    @XmlElement(name = "ValDt")
    private ValueDate valueDate;
    @XmlElement(name = "NtryDtls")
    private EntryDetails entryDetails;
}
