package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "ValDt")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValueDate {
    @XmlElement(name = "Dt")
    private String date;
}
