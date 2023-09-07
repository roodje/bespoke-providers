package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "GrpHdr")
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupHeader {
    @XmlElement(name = "MsgPgntn")
    private MessagePagination messagePagination;
}
