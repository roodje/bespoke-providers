package com.yolt.providers.stet.labanquepostalegroup.labanquepostale;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.providerdetail.PisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.DynamicFieldOptions;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import com.yolt.providers.common.providerdetail.dto.PaymentMethod;
import com.yolt.providers.common.providerdetail.dto.PisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.providerdetail.dto.DynamicFieldNames.CREDITOR_AGENT_BIC;
import static com.yolt.providers.common.providerdetail.dto.DynamicFieldNames.CREDITOR_AGENT_NAME;
import static java.util.List.of;

@Service
public class LaBanquePostalePisDetailsProvider implements PisDetailsProvider {

    private static final List<PisSiteDetails> PIS_SITE_DETAILS = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString("6306b446-a24f-11e9-a2a3-2a2ae2dbcce4"))
                    .providerKey("LA_BANQUE_POSTALE")
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Map.of(CREDITOR_AGENT_BIC, new DynamicFieldOptions(true),
                            CREDITOR_AGENT_NAME, new DynamicFieldOptions(true)))
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<PisSiteDetails> getPisSiteDetails() {
        return PIS_SITE_DETAILS;
    }
}
