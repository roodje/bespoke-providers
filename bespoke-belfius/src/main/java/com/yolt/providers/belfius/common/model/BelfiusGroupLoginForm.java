package com.yolt.providers.belfius.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BelfiusGroupLoginForm {

    private AuthenticationMeansReference authenticationMeansReference;
    private String redirectUrl;
}
