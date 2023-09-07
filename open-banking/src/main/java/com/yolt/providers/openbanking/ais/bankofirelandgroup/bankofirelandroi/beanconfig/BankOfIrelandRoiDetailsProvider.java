package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofirelandroi.beanconfig;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.IE;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.*;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class BankOfIrelandRoiDetailsProvider implements AisDetailsProvider {
    public static final String BANK_OF_IRELAND_ROI_SITE_ID = "640e085a-7d4e-48f2-9844-2fea712834ee";
    public static final String BANK_OF_IRELAND_ROI_PROVIDER_KEY = "BANK_OF_IRELAND_ROI";
    public static final String BANK_OF_IRELAND_ROI_DISPLAY_NAME = "Bank of Ireland (ROI)";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(BANK_OF_IRELAND_ROI_SITE_ID, BANK_OF_IRELAND_ROI_DISPLAY_NAME, BANK_OF_IRELAND_ROI_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT), of(IE))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
