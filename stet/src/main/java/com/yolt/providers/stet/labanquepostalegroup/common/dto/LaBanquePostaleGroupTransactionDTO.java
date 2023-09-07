package com.yolt.providers.stet.labanquepostalegroup.common.dto;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface LaBanquePostaleGroupTransactionDTO extends StetTransactionDTO {

    @Override
    @JsonPath("$.remittanceInformation")
    List<String> getUnstructuredRemittanceInformation();
}
