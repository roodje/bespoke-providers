package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "Amt")
@XmlAccessorType(XmlAccessType.FIELD)
public class Amount {
    @XmlValue
    private String value;
    @XmlAttribute(name = "Ccy", required = true)
    private String currency;
}
