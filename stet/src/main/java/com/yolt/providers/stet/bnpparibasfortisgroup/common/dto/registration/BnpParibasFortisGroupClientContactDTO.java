package com.yolt.providers.stet.bnpparibasfortisgroup.common.dto.registration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BnpParibasFortisGroupClientContactDTO {

    @Builder.Default
    private String contactType = "Business";
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}