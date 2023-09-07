package com.yolt.providers.monorepogroup.libragroup.libra;

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
public class LibraDetailsProvider implements AisDetailsProvider {

    public static final String LIBRA_SITE_ID = "43ebaaf4-a7f0-49b4-a9bd-e879fccbb326";
    public static final String LIBRA_PROVIDER_KEY = "LIBRA_BANK";
    public static final String LIBRA_PROVIDER_NAME = "Libra Bank";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(LIBRA_SITE_ID, LIBRA_PROVIDER_NAME, LIBRA_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(RO))
                    .groupingBy(LIBRA_PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(REDIRECT)))
                    .loginRequirements(of(REDIRECT))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}