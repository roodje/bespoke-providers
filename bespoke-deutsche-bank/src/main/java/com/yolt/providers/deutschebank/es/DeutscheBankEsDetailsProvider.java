package com.yolt.providers.deutschebank.es;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.ES;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class DeutscheBankEsDetailsProvider implements AisDetailsProvider {

    private static final String DEUTSCHE_BANK_ES_SITE_ID = "045040dc-842b-42a9-995a-0291ca31095d";
    private static final String DEUTSCHE_BANK_ES_PROVIDER_KEY = "DEUTSCHE_BANK_ES";
    private static final String DEUTSCHE_BANK_ES_PROVIDER_NAME = "Deutsche Bank (ES)";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(DEUTSCHE_BANK_ES_SITE_ID, DEUTSCHE_BANK_ES_PROVIDER_NAME, DEUTSCHE_BANK_ES_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(ES))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT, LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.REDIRECT, LoginRequirement.FORM))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}