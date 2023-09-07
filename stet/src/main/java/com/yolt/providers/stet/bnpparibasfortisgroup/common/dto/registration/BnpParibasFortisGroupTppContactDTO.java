package com.yolt.providers.stet.bnpparibasfortisgroup.common.dto.registration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BnpParibasFortisGroupTppContactDTO {

    private String phone;
    private String email;
    private String website;
}