package com.yolt.providers.stet.bnpparibasfortisgroup.common.mapper.registration;

import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.auth.BnpParibasFortisAuthenticationMeans;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.dto.registration.BnpParibasFortisGroupClientContactDTO;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.dto.registration.BnpParibasFortisGroupRegistrationRequestDTO;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.dto.registration.BnpParibasFortisGroupTppContactDTO;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.mapper.registration.RegistrationRequestMapper;

import java.util.Collections;

public class BnpParibasFortisGroupRegistrationRequestMapper implements RegistrationRequestMapper {

    @Override
    public Object mapToRegistrationRequest(RegistrationRequest registrationRequest) {
        BnpParibasFortisAuthenticationMeans authMeans = (BnpParibasFortisAuthenticationMeans) registrationRequest.getAuthMeans();
        return BnpParibasFortisGroupRegistrationRequestDTO.builder()
                .clientName(authMeans.getClientName())
                .clientDescription(authMeans.getClientDescription())
                .clientVersion("1")
                .clientContacts(BnpParibasFortisGroupClientContactDTO.builder()
                        .firstName(authMeans.getContactFirstName())
                        .lastName(authMeans.getContactLastName())
                        .email(authMeans.getClientEmail())
                        .phone(authMeans.getContactPhone())
                        .build())
                .tppContacts(BnpParibasFortisGroupTppContactDTO.builder()
                        .phone(authMeans.getContactPhone())
                        .email(authMeans.getClientEmail())
                        .website(authMeans.getClientWebsiteUri())
                        .build())
                .redirectUris(Collections.singletonList(registrationRequest.getRedirectUrl()))
                .uri(authMeans.getClientWebsiteUri())
                .build();
    }
}
