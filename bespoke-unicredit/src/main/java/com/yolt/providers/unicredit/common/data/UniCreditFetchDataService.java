package com.yolt.providers.unicredit.common.data;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.unicredit.common.util.ProviderInfo;

public interface UniCreditFetchDataService {
    DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData, final ProviderInfo providerInfo) throws ProviderFetchDataException, TokenInvalidException;
}
