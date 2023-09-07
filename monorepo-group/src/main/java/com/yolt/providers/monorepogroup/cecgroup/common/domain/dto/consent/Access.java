package com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.consent;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Access implements Serializable {

    private String allPsd2;
}
