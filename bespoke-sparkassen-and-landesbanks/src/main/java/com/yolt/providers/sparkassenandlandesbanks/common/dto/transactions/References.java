package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "Refs")
@XmlAccessorType(XmlAccessType.FIELD)
public class References {
    @XmlElement(name = "EndToEndId")
    private String endToEndId;
}
