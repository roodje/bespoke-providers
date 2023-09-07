package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "CreditorAccountIdentification")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreditorAccountIdentification {
    @XmlElement(name = "IBAN")
    private String iban;
}
