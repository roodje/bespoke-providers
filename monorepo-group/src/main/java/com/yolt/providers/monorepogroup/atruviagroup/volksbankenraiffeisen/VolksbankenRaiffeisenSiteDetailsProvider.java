package com.yolt.providers.monorepogroup.atruviagroup.volksbankenraiffeisen;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.DE;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
class VolksbankenRaiffeisenSiteDetailsProvider implements AisDetailsProvider {

    public static final String VOLKSBANKEN_RAIFFEISEN_SITE_ID = "c9f82c9e-f05f-4e5a-92fe-e86b9f6de82b";
    public static final String VOLKSBANKEN_RAIFFEISEN_PROVIDER_KEY = "VOLKSBANKEN_RAIFFEISEN";
    public static final String VOLKSBANKEN_RAIFFEISEN_DISPLAY_NAME = "Volksbanken Raiffeisen";

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return of(
                site(VOLKSBANKEN_RAIFFEISEN_SITE_ID, //site-id
                        VOLKSBANKEN_RAIFFEISEN_DISPLAY_NAME, //provider display name
                        VOLKSBANKEN_RAIFFEISEN_PROVIDER_KEY, //provider identifier
                        DIRECT_CONNECTION, //provider type
                        of(STATE), //provider behavior
                        of(CURRENT_ACCOUNT), //list of supported account types
                        of(DE)) //country list
                        .groupingBy(VOLKSBANKEN_RAIFFEISEN_DISPLAY_NAME) //we usualy set it as the same value as provider display name
                        .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM))) //usage types
                        .loginRequirements(of(LoginRequirement.FORM)) // login requirements
                        .consentExpiryInDays(90)
                        .build()
        );
    }
}
