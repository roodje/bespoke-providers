package com.yolt.providers.stet.bpcegroup.common.service.pec;

import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentHttpRequestInvoker;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentPreExecutionResultMapperV2;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentProviderStateExtractor;
import com.yolt.providers.stet.generic.service.pec.confirmation.submit.StetSubmitPaymentPreExecutionResultMapperV2;
import org.springframework.http.HttpMethod;

import java.util.function.Function;
import java.util.function.Supplier;

public class BpceSubmitPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> extends StetSubmitPaymentPreExecutionResultMapperV2<TokenPaymentPreExecutionResult> {
    public BpceSubmitPaymentPreExecutionResultMapper(AuthenticationMeansSupplier authenticationMeansSupplier, StetPaymentProviderStateExtractor providerStateExtractor, ProviderIdentification providerIdentification, SepaTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper, SepaTokenPaymentHttpRequestInvoker tokenHttpRequestInvoker, Supplier paymentConfirmationTemplateSupplier, HttpClientFactory httpClientFactory, Function<Region, String> baseUrlFunction, DefaultProperties properties) {
        super(authenticationMeansSupplier, providerStateExtractor, providerIdentification, tokenPaymentPreExecutionResultMapper, tokenHttpRequestInvoker, paymentConfirmationTemplateSupplier, httpClientFactory, baseUrlFunction, properties);
    }

    @Override
    protected HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
