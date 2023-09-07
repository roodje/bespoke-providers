package com.yolt.providers.stet.lclgroup.common.pec.initiate;

import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentHttpRequestInvoker;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentPreExecutionResultMapperV2;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiateSinglePaymentPreExecutionResultMapperV3;

import java.util.function.Function;
import java.util.function.Supplier;


public class LclGroupInitiateSinglePaymentPreExecutionResultMapper extends StetInitiateSinglePaymentPreExecutionResultMapperV3 {

    public LclGroupInitiateSinglePaymentPreExecutionResultMapper(AuthenticationMeansSupplier authenticationMeansSupplier,
                                                                 ProviderIdentification providerIdentification,
                                                                 SepaTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper,
                                                                 SepaTokenPaymentHttpRequestInvoker tokenHttpRequestInvoker,
                                                                 Supplier paymentInitiationEndpointSupplier,
                                                                 HttpClientFactory httpClientFactory,
                                                                 Function<Region, String> baseUrlFunction,
                                                                 DefaultProperties properties) {
        super(authenticationMeansSupplier, providerIdentification, tokenPaymentPreExecutionResultMapper, tokenHttpRequestInvoker,
                paymentInitiationEndpointSupplier, httpClientFactory, baseUrlFunction, properties);
    }

    @Override
    public StetInitiatePreExecutionResult map(InitiatePaymentRequest request) {
        var result = super.map(request);
        result.setBaseClientRedirectUrl(result.getBaseClientRedirectUrl() + "/");
        return result;
    }
}

