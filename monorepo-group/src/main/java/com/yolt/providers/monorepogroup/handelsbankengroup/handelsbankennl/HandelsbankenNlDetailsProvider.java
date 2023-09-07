package com.yolt.providers.monorepogroup.handelsbankengroup.handelsbankennl;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.RO;
import static com.yolt.providers.common.providerdetail.dto.LoginRequirement.REDIRECT;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class HandelsbankenNlDetailsProvider implements AisDetailsProvider {
    public static final String HANDELSBANKEN_NL_SITE_ID = "3bab6974-308a-474b-b756-9cb9aefc9766";
    public static final String HANDELSBANKEN_NL_PROVIDER_KEY = "HANDELSBANKEN_NL";
    public static final String HANDELSBANKEN_NL_PROVIDER_NAME = "Handelsbanken";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(HANDELSBANKEN_NL_SITE_ID, HANDELSBANKEN_NL_PROVIDER_NAME, HANDELSBANKEN_NL_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(RO))
                    .groupingBy(HANDELSBANKEN_NL_PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(REDIRECT)))
                    .loginRequirements(of(REDIRECT))
                    .consentExpiryInDays(89)
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
