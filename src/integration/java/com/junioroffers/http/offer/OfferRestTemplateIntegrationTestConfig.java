package com.junioroffers.http.offer;

import com.junioroffers.domain.offer.OfferFetchable;
import com.junioroffers.infrastructure.offer.http.OfferHttpClientConfig;
import org.springframework.web.client.RestTemplate;

import static com.junioroffers.BaseIntegrationTest.WIRE_MOCK_HOST;

public class OfferRestTemplateIntegrationTestConfig extends OfferHttpClientConfig {

    public OfferFetchable remoteOfferFetchableClient(int port, int connectionTimeout, int readTimeout) {
        final RestTemplate restTemplate = restTemplate(connectionTimeout, readTimeout, restTemplateResponseErrorHandler());
        return remoteOfferClient(restTemplate, WIRE_MOCK_HOST, port);
    }

}
