package com.yolt.providers.commerzbankgroup.commerzbank;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.DE;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

class CommerzbankSiteDetailsProvider implements AisDetailsProvider {

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return of(
                site("a8d42732-2028-411d-bc1e-4980e23dbdb6", //site-id
                        "Commerzbank AG", //provider display name
                        "COMMERZBANK", //provider identifier
                        DIRECT_CONNECTION, //provider type
                        of(STATE), //provider behavior
                        of(CURRENT_ACCOUNT), //list of supported account types
                        of(DE)) //country list
                        .groupingBy("Commerzbank") //we usualy set it as yhe same value as provider display name
                        .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT))) //usage types
                        .loginRequirements(of(LoginRequirement.REDIRECT)) // login requirements
                        .build()
        );
    }
}
