package com.yolt.providers.cbiglobe.common.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.yolt.providers.cbiglobe.common.util.CbiGlobeDateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Data
@AllArgsConstructor
public class CbiGlobeAccessMeansDTO {

    private Instant updated;
    private String accessToken;
    private Instant accessTokenExpiration;
    private String consentId;
    private Instant consentExpiration;
    private List<ProviderAccountDTO> cachedAccounts;
    @JsonDeserialize(as = HashMap.class)
    private Map<String, ProviderAccountDTO> consentedAccounts;
    private Integer currentlyProcessAccountNumber;
    private String bank;
    private String callBackUrl;


    public CbiGlobeAccessMeansDTO(Token token, Integer validityDays, String callBackUrl, Clock clock) {
        this.updated = Instant.now(clock);
        this.accessToken = token.getAccessToken();
        this.accessTokenExpiration = token.getExpiresIn();
        this.consentExpiration = CbiGlobeDateUtil.calculateValidityDateTime(validityDays, clock);
        this.callBackUrl = callBackUrl;
        this.cachedAccounts = Collections.emptyList();
        this.consentedAccounts = new HashMap<>();
        this.currentlyProcessAccountNumber = 0;
    }

    public boolean hasNoCachedAccounts() {
        return CollectionUtils.isEmpty(cachedAccounts);
    }

    public void addConsentForAccount(String consentId, ProviderAccountDTO account) {
        consentedAccounts.put(consentId, account);
    }

    public void removeAccountFromConsentedAccountList(String consentId) {
        consentedAccounts.remove(consentId);
    }

    public AccessMeansDTO toAccessMeansDTO(UUID userId, String providerState) {
        return new AccessMeansDTO(userId, providerState, toDate(updated), toDate(consentExpiration));
    }

    private Date toDate(Instant value) {
        return new Date(value.toEpochMilli());
    }
}
