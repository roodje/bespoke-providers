package com.yolt.providers.consorsbankgroup.common.ais;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultAccessMeans {

    @NonNull
    private String consentId;
}
