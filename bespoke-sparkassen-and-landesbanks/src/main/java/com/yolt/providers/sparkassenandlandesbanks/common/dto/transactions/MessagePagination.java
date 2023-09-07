package com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = "MsgPgntn")
@XmlAccessorType(XmlAccessType.FIELD)
public class MessagePagination {
    @XmlElement(name = "PgNb")
    private int pageNumber;
    @XmlElement(name = "LastPgInd")
    private boolean lastPageInidication;
}
