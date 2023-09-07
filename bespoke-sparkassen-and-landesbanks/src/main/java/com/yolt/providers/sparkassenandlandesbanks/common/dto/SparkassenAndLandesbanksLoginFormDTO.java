package com.yolt.providers.sparkassenandlandesbanks.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkassenAndLandesbanksLoginFormDTO {
    private AuthenticationMeansReference authenticationMeansReference;
    private String redirectUrl;
}