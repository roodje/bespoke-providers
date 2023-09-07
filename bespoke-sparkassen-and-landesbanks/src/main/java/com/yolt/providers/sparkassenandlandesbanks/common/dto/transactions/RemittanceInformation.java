package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "RmtInf")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemittanceInformation {
    @XmlElement(name = "Ustrd")
    private String unstructured;
}
