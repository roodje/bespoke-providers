package com.yolt.providers.stet.generic.mapper.registration;

import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;

public interface RegistrationRequestMapper {

    Object mapToRegistrationRequest(RegistrationRequest registrationRequest);

    default Object mapToUpdateRegistrationRequest(RegistrationRequest registrationRequest) {
        throw new UnsupportedOperationException("Not available");
    }
}
