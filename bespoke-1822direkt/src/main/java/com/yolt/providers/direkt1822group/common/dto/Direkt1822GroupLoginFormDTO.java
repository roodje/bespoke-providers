package com.yolt.providers.direkt1822group.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Direkt1822GroupLoginFormDTO {

    private AuthenticationMeansReference authenticationMeansReference;
    private String redirectUrl;
}
