package com.yolt.providers.stet.boursoramagroup.common.dto;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface BoursoramaGroupTransactionDTO extends StetTransactionDTO {
    
    @Override
    @JsonPath("$.remittanceInformation")
    List<String> getUnstructuredRemittanceInformation();
}
