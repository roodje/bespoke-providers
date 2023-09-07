package com.yolt.providers.monorepogroup.raiffeisenatgroup.raiffeisenat;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.AT;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.DE;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static com.yolt.providers.monorepogroup.raiffeisenatgroup.raiffeisenat.RaiffeisenBeanConfig.PROVIDER_DISPLAY_NAME;
import static com.yolt.providers.monorepogroup.raiffeisenatgroup.raiffeisenat.RaiffeisenBeanConfig.PROVIDER_IDENTIFIER;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class RaiffeisenAtDetailsProvider implements AisDetailsProvider {

    public static final String RAIFFEISEN_AT_SITE_ID = "1da633b1-e54e-4d59-824f-03f31296e199";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(RAIFFEISEN_AT_SITE_ID, PROVIDER_DISPLAY_NAME, PROVIDER_IDENTIFIER, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(AT, DE))
                    .groupingBy(PROVIDER_DISPLAY_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .consentExpiryInDays(90)
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
