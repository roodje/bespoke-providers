package com.yolt.providers.stet.cicgroup.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CicGroupJsonWebKeySet {

    private List<CicGroupCertificateJsonWebKey> keys;
}
