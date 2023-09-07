package com.yolt.providers.openbanking.ais.barclaysgroup.common.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

@Data
@RequiredArgsConstructor
public class BarclaysLoginFormDTO {

    private final AuthenticationMeansReference authenticationMeansReference;
    private final String redirectUrl;
}
