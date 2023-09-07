package com.yolt.providers.redsys.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedsysGroupLoginFormDTO {
    private String redirectUrl;
    private String state;
}