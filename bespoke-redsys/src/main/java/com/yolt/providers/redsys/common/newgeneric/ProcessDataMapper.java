package com.yolt.providers.redsys.common.newgeneric;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;

public interface ProcessDataMapper<T extends SerializableConsentProcessData> {
    ConsentProcessArguments<T> map(UrlGetLoginRequest urlGetLogin);

    ConsentProcessArguments<T> map(UrlCreateAccessMeansRequest urlCreateAccessMeans);

    T deserializeState(String providerState);

    String serializeState(ConsentProcessArguments<T> processArguments);

    String serializeState(T processData);
}
