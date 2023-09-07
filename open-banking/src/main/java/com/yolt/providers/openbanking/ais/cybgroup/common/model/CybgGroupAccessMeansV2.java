package com.yolt.providers.openbanking.ais.cybgroup.common.model;

import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CybgGroupAccessMeansV2 extends AccessMeans {

    private List<OBAccount6> cachedAccounts;

    public CybgGroupAccessMeansV2(AccessMeans accessMeans, List<OBAccount6> cachedAccounts) {
        super(accessMeans.getCreated(),
                accessMeans.getUserId(),
                accessMeans.getAccessToken(),
                accessMeans.getRefreshToken(),
                accessMeans.getExpireTime(),
                accessMeans.getUpdated(),
                accessMeans.getRedirectUri());
        this.cachedAccounts = cachedAccounts;
    }
}

