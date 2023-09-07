package com.yolt.providers.stet.bnpparibasfortisgroup.common.dto.registration;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BnpParibasFortisGroupRegistrationRequestDTO {

    private String clientName;
    private String clientDescription;
    private String clientVersion;
    private List<String> redirectUris;
    private String uri;
    private BnpParibasFortisGroupClientContactDTO clientContacts;
    private BnpParibasFortisGroupTppContactDTO tppContacts;
}
