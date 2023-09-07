package com.yolt.providers.stet.generic.service.fetchdata.rest.header;

import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public interface FetchDataHttpHeadersFactory {

    HttpHeaders createFetchDataHeaders(String endpoint, DataRequest dataRequest, HttpMethod method);
}
