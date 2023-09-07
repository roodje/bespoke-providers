package com.yolt.providers.openbanking.ais.amexgroup.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class AmexLoginInfoState extends LoginInfoState {

    String codeVerifier;

    public AmexLoginInfoState(List<String> permissions) {
        super(permissions);
    }
}
