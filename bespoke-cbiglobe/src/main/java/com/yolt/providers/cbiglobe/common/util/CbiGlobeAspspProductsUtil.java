package com.yolt.providers.cbiglobe.common.util;

import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.marker.Markers.append;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CbiGlobeAspspProductsUtil {

    private static final Marker RDD_MARKER = append("raw-data", "true");

    private static final String TPP_ASPSP_PRODUCT_CODES_URL_TEMPLATE = "https://%s/platform/enabler/psd2orchestrator/tpp/aspsps/2.3.2/";
    private static final String ASPSP_CODE_QUERY_PARAM = "aspsp_code";

    public static void fetchAndLogAspspsProductCodesIfEmpty(RestTemplate restTemplate, String accessToken, CbiGlobeBaseProperties properties) {
        for (AspspData aspspData : getAspspDataWithEmptyProductCodes(properties)) {
            fetchAndLogAspspProductCodes(restTemplate, properties, accessToken, aspspData);
        }
    }

    private static List<AspspData> getAspspDataWithEmptyProductCodes(CbiGlobeBaseProperties properties) {
        return properties.getAspsps()
                .stream()
                .filter(AspspData::isEmptyProductCode)
                .collect(Collectors.toList());
    }

    public static void fetchAndLogAspspProductCodes(RestTemplate restTemplate,
                                                    CbiGlobeBaseProperties properties,
                                                    String accessToken,
                                                    AspspData aspspData) {
        try {
            String aspspCode = aspspData.getCode();
            HttpHeaders headers = CbiGlobeHttpHeaderUtil.getAspspProductsHeaders(accessToken);

            String url = UriComponentsBuilder.fromUriString(getTppAspspProductCodesUrl(properties.getBaseUrl()))
                    .queryParam(ASPSP_CODE_QUERY_PARAM, aspspCode)
                    .toUriString();

            Object aspspProductCodes = restTemplate
                    .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class)
                    .getBody();

            log.debug(RDD_MARKER, "ASPSP: {}, ASPSP Product codes: {}", aspspCode, aspspProductCodes);
        } catch (RestClientResponseException e) {
            log.debug(RDD_MARKER, "Failed to fetch ASPSP Product codes: HTTP: {}, ASPSP: {}, Response: {}",
                    e.getRawStatusCode(), aspspData.getCode(), e.getResponseBodyAsString());
        }
    }

    private static String getTppAspspProductCodesUrl(String baseUrl) {
        return String.format(TPP_ASPSP_PRODUCT_CODES_URL_TEMPLATE, URI.create(baseUrl).getAuthority());
    }
}

