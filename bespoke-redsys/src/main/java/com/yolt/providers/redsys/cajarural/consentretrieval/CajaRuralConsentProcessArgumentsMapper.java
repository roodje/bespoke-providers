package com.yolt.providers.redsys.cajarural.consentretrieval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.redsys.cajarural.RuralBank;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.newgeneric.ConsentProcessArguments;
import com.yolt.providers.redsys.common.newgeneric.ProcessDataMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static com.yolt.providers.redsys.cajarural.consentretrieval.CajaRuralConsentRetrievalProcess.REGION_FIELD;

@AllArgsConstructor
public class CajaRuralConsentProcessArgumentsMapper implements ProcessDataMapper<CajaRuralSerializableConsentProcessData> {
    private final ObjectMapper mapper;
    private final String providerIdentifier;

    @Override
    public ConsentProcessArguments<CajaRuralSerializableConsentProcessData> map(UrlGetLoginRequest urlGetLogin) {
        return new ConsentProcessArguments<>(
                RedsysAuthenticationMeans.fromAuthenticationMeans(
                        urlGetLogin.getAuthenticationMeans(),
                        providerIdentifier),
                new CajaRuralSerializableConsentProcessData(),
                urlGetLogin.getRestTemplateManager(),
                urlGetLogin.getSigner(),
                urlGetLogin.getPsuIpAddress(),
                null,
                urlGetLogin.getState(),
                null,
                null);
    }

    @Override
    public ConsentProcessArguments<CajaRuralSerializableConsentProcessData> map(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        CajaRuralSerializableConsentProcessData processData = deserializeState(urlCreateAccessMeans.getProviderState());
        if (StringUtils.isEmpty(processData.getAspspName()) && !StringUtils.isEmpty(urlCreateAccessMeans.getFilledInUserSiteFormValues().get(REGION_FIELD))) {
            processData.setAspspName(RuralBank.valueOf(urlCreateAccessMeans.getFilledInUserSiteFormValues().get(REGION_FIELD)).name());
        }

        return new ConsentProcessArguments<>(
                RedsysAuthenticationMeans.fromAuthenticationMeans(
                        urlCreateAccessMeans.getAuthenticationMeans(), providerIdentifier),
                processData,
                urlCreateAccessMeans.getRestTemplateManager(),
                urlCreateAccessMeans.getSigner(),
                urlCreateAccessMeans.getPsuIpAddress(),
                urlCreateAccessMeans.getUserId(),
                urlCreateAccessMeans.getState(),
                urlCreateAccessMeans.getRedirectUrlPostedBackFromSite(),
                urlCreateAccessMeans.getBaseClientRedirectUrl());
    }

    @Override
    public CajaRuralSerializableConsentProcessData deserializeState(String providerState) {
        try {
            return mapper.readValue(providerState, CajaRuralSerializableConsentProcessData.class);
        } catch (IOException e) {
            throw new MissingDataException("Missing provider state");
        }
    }

    @Override
    public String serializeState(ConsentProcessArguments<CajaRuralSerializableConsentProcessData> processArguments) {
        return serializeState(processArguments.getConsentProcessData());
    }

    @Override
    public String serializeState(CajaRuralSerializableConsentProcessData processData) {
        try {
            return mapper.writeValueAsString(processData);
        } catch (
                JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize process data.");
        }
    }
}
