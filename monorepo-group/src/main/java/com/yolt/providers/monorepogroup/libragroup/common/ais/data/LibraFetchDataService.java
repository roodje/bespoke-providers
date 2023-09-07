package com.yolt.providers.monorepogroup.libragroup.common.ais.data;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupAccessMeans;

public interface LibraFetchDataService {
    DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData,
                                   LibraGroupAccessMeans accessMeans,
                                   LibraGroupAuthenticationMeans.SigningData signingData,
                                   Signer signer) throws TokenInvalidException, ProviderFetchDataException;
}
