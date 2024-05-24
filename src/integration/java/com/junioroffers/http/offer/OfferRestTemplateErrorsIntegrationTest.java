package com.junioroffers.http.offer;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.junioroffers.SampleJobOfferResponse;
import com.junioroffers.domain.offer.OfferFetchable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.server.ResponseStatusException;


import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class OfferRestTemplateErrorsIntegrationTest implements SampleJobOfferResponse {

    @RegisterExtension
    public static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    OfferFetchable offerFetchable = new OfferRestTemplateIntegrationTestConfig().remoteOfferFetchableClient(
            wireMockServer.getPort(),
            1000,
            1000);

    @Test
    public void should_throw_exception_500_when_fault_connection_reset_by_peer() {
        //given
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withHeader("Content-Type", "application/json")
                        .withFault(Fault.CONNECTION_RESET_BY_PEER)));

        //when
        Throwable throwable = catchThrowable(() -> offerFetchable.fetchOffers());

        //then
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(throwable.getMessage()).isEqualTo("500 INTERNAL_SERVER_ERROR");
    }

    @Test
    void should_throw_exception_500_when_fault_empty_response() {
        //given
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withHeader("Content-Type", "application/json")
                        .withFault(Fault.EMPTY_RESPONSE)));
        //when
        Throwable throwable = catchThrowable(() -> offerFetchable.fetchOffers());

        //then
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(throwable.getMessage()).isEqualTo("500 INTERNAL_SERVER_ERROR");
    }


    @Test
    void should_throw_exception_500_when_fault_malformed_response_chunk() {
        //given
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withHeader("Content-Type", "application/json")
                        .withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
        //when
        Throwable throwable = catchThrowable(() -> offerFetchable.fetchOffers());

        //then
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(throwable.getMessage()).isEqualTo("500 INTERNAL_SERVER_ERROR");
    }

    @Test
    void should_throw_exception_500_when_fault_random_data_then_close() {
        //given
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withHeader("Content-Type", "application/json")
                        .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
        //when
        Throwable throwable = catchThrowable(() -> offerFetchable.fetchOffers());

        //then
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(throwable.getMessage()).isEqualTo("500 INTERNAL_SERVER_ERROR");
    }

    @Test
    void should_throw_exception_404_when_http_service_returning_not_found_status() {
        // given
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.SC_NOT_FOUND))
        );

        // when
        Throwable throwable = catchThrowable(() -> offerFetchable.fetchOffers());

        // then
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(throwable.getMessage()).isEqualTo("404 NOT_FOUND");
    }

    @Test
    void should_throw_exception_401_when_http_service_returning_unauthorized_status() {
        // given
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.SC_UNAUTHORIZED))
        );

        // when
        Throwable throwable = catchThrowable(() -> offerFetchable.fetchOffers());

        // then
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(throwable.getMessage()).isEqualTo("401 UNAUTHORIZED");
    }

    @Test
    void should_throw_exception_204_when_status_is_204_no_content() {
        // given
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.SC_NO_CONTENT)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                []
                                  """.trim()
                        )));

        // when
        Throwable throwable = catchThrowable(() -> offerFetchable.fetchOffers());

        // then
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(throwable.getMessage()).isEqualTo("204 NO_CONTENT");
    }

    @Test
    void should_throw_exception_500_when_response_delay_is_5000_ms_and_client_has_1000ms_read_timeout() {
        // given
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withHeader("Content-Type", "application/json")
                        .withFault(Fault.EMPTY_RESPONSE)
                        .withFixedDelay(5000)));

        // when
        Throwable throwable = catchThrowable(() -> offerFetchable.fetchOffers());

        // then
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(throwable.getMessage()).isEqualTo("500 INTERNAL_SERVER_ERROR");
    }


}
