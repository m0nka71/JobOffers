package com.junioroffers.features;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.junioroffers.BaseIntegrationTest;
import com.junioroffers.SampleJobOfferResponse;
import com.junioroffers.domain.loginandregister.dto.RegistrationResultDto;
import com.junioroffers.domain.offer.dto.OfferResponseDto;
import com.junioroffers.infrastructure.loginandregister.controller.dto.JwtResponseDto;
import com.junioroffers.infrastructure.offer.scheduler.HttpOffersScheduler;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class UserWantsToSeeOffersIntegrationTest extends BaseIntegrationTest implements SampleJobOfferResponse {

    @Autowired
    HttpOffersScheduler httpOffersScheduler;

    @Container
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

    @DynamicPropertySource
    public static void propertyOverride(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("offer.http.client.config.uri", () -> WIRE_MOCK_HOST);
        registry.add("offer.http.client.config.port", () -> wireMockServer.getPort());
    }

    @Test
    public void user_want_to_see_offers_but_have_to_be_logged_in_and_external_server_should_have_some_offers() throws Exception {
        // step 1: there are no offers in external HTTP server
        // given && when && then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithZeroOffersJson())));


        // step 2: scheduler ran 1st time and made GET to external server and system added 0 offers to database
        // given && when
        List<OfferResponseDto> newOffers = httpOffersScheduler.fetchAllOffersAndSaveAllIfNotExists();
        // then
        assertThat(newOffers).isEmpty();


        //step 3: user tried to get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned UNAUTHORIZED(401)
        // given & when
        ResultActions failedLoginRequest = mockMvc.perform(post("/token")
                .content("""
                        {
                        "username": "someUser",
                        "password": "somePassword"
                        }
                        """.trim())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        failedLoginRequest
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("""
                        {
                          "message": "Bad Credentials",
                          "status": "UNAUTHORIZED"
                        }
                        """.trim()));


        //step 4: user made GET /offers with no jwt token and system returned UNAUTHORIZED(401)
        // given & when
        ResultActions failedGetOffersRequest = mockMvc.perform(get("/offers")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        failedGetOffersRequest.andExpect(status().isForbidden());


        //step 5: user made POST /register with username=someUser, password=somePassword and system registered user with status CREATED(201)
        // given & when
        ResultActions registerAction = mockMvc.perform(post("/register")
                .content("""
                        {
                        "username": "someUser",
                        "password": "somePassword"
                        }
                        """.trim())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        MvcResult registerActionResult = registerAction.andExpect(status().isCreated()).andReturn();
        String registerActionResultJson = registerActionResult.getResponse().getContentAsString();
        RegistrationResultDto registrationResultDto = objectMapper.readValue(registerActionResultJson, RegistrationResultDto.class);
        assertAll(
                () -> assertThat(registrationResultDto.username()).isEqualTo("someUser"),
                () -> assertThat(registrationResultDto.created()).isTrue(),
                () -> assertThat(registrationResultDto.id()).isNotNull()
        );

        //step 6: user tried to get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned OK(200) and jwttoken=AAAA.BBBB.CCC
        //given && when
        ResultActions successLoginRequest = mockMvc.perform(post("/token")
                .content("""
                        {
                        "username": "someUser",
                        "password": "somePassword"
                        }
                        """.trim())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );

        //then
        MvcResult successResult = successLoginRequest.andExpect(status().isOk()).andReturn();
        String json = successResult.getResponse().getContentAsString();
        JwtResponseDto jwtResponseDto = objectMapper.readValue(json, JwtResponseDto.class);
        String token = jwtResponseDto.token();
        assertAll(
                () -> assertThat(jwtResponseDto.username()).isEqualTo("someUser"),
                () -> assertThat(token).matches(Pattern.compile("^([A-Za-z0-9-_=]+\\.)+([A-Za-z0-9-_=])+\\.?$"))
        );

        //step 7: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 0 offers
        //given && when
        ResultActions performGetOffers = mockMvc.perform(get("/offers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        // then
        MvcResult mvcResult2 = performGetOffers.andExpect(status().isOk()).andReturn();
        String jsonWithOffers = mvcResult2.getResponse().getContentAsString();
        List<OfferResponseDto> offers = objectMapper.readValue(jsonWithOffers, new TypeReference<>() {
        });
        assertThat(offers).isEmpty();

        //step 8: there are 2 new offers in external HTTP server
        //given && when && then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithTwoOffersJson())));

        //step 9: scheduler ran 2nd time and made GET to external server and system added 2 new offers with ids: 1000 and 2000 to database
        //given && when
        List<OfferResponseDto> twoOfferResponseDtos = httpOffersScheduler.fetchAllOffersAndSaveAllIfNotExists();
        //then
        assertThat(twoOfferResponseDtos).hasSize(2);

        //step 10: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 2 offers with ids: 1000 and 2000
        //given && when
        ResultActions performGetTwoOffers = mockMvc.perform(get("/offers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        //then
        String jsonWithTwoOffers = performGetTwoOffers.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<OfferResponseDto> parsedJsonWithTwoOffers = objectMapper.readValue(jsonWithTwoOffers, new TypeReference<>() {
        });
        assertThat(parsedJsonWithTwoOffers).hasSize(2);

        OfferResponseDto firstExpectedOffer = twoOfferResponseDtos.get(0);
        OfferResponseDto secondExpectedOffer = twoOfferResponseDtos.get(1);
        assertThat(twoOfferResponseDtos).containsExactlyInAnyOrder(
                new OfferResponseDto(firstExpectedOffer.id(), firstExpectedOffer.companyName(), firstExpectedOffer.position(), firstExpectedOffer.salary(), firstExpectedOffer.offerUrl()),
                new OfferResponseDto(secondExpectedOffer.id(), secondExpectedOffer.companyName(), secondExpectedOffer.position(), secondExpectedOffer.salary(), secondExpectedOffer.offerUrl()
                ));

        //step 11: user made GET /offers/9999 and system returned NOT_FOUND(404) with message “Offer with id 9999 not found”
        //given && when
        ResultActions performOffersWithNoExistingId = mockMvc.perform(get("/offers/9999")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        //then
        performOffersWithNoExistingId.andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                        "message":  "Offer with id 9999 not found",
                        "status": "NOT_FOUND"
                        }
                        """.trim()));

        //step 12: user made GET /offers/1000 and system returned OK(200) with offer
        //given && when
        String offerIdAddedToDatabase = firstExpectedOffer.id();
        ResultActions performGetOfferWithExistingId = mockMvc.perform(get("/offers/" + offerIdAddedToDatabase)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        //then
        String jsonWithExistingOfferId = performGetOfferWithExistingId.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OfferResponseDto offerResponseDtoByOfferUrl = objectMapper.readValue(jsonWithExistingOfferId, OfferResponseDto.class);
        assertThat(offerResponseDtoByOfferUrl).isEqualTo(firstExpectedOffer);

        //step 13: there are 2 new offers in external HTTP server
        // given && when && then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithFourOffersJson())));

        //step 14: scheduler ran 3rd time and made GET to external server and system added 2 new offers with ids: 3000 and 4000 to database
        // given && when
        List<OfferResponseDto> nextTwoNewOffers = httpOffersScheduler.fetchAllOffersAndSaveAllIfNotExists();
        // then
        assertThat(nextTwoNewOffers).hasSize(2);

        //step 15: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 4 offers with ids: 1000,2000, 3000 and 4000
        //given && when
        ResultActions performGetFourOffers = mockMvc.perform(get("/offers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        //then
        String jsonWithFourOffers = performGetFourOffers.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<OfferResponseDto> fourOffers = objectMapper.readValue(jsonWithFourOffers, new TypeReference<>() {
        });
        assertThat(fourOffers).hasSize(4);
        OfferResponseDto thirdExpectedOffer = twoOfferResponseDtos.get(0);
        OfferResponseDto fourthExpectedOffer = twoOfferResponseDtos.get(1);
        assertThat(fourOffers).contains(
                new OfferResponseDto(thirdExpectedOffer.id(), thirdExpectedOffer.companyName(), thirdExpectedOffer.position(), thirdExpectedOffer.salary(), thirdExpectedOffer.offerUrl()),
                new OfferResponseDto(fourthExpectedOffer.id(), fourthExpectedOffer.companyName(), fourthExpectedOffer.position(), fourthExpectedOffer.salary(), fourthExpectedOffer.offerUrl()
                ));

        //step 16: user made POST /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and offers as body and system returned CREATED(201) with saved offer
        //given && when
        ResultActions performOnePostOffer = mockMvc.perform(post("/offers")
                .header("Authorization", "Bearer " + token)
                .content("""
                        {
                        "companyName": "ExampleCompany",
                        "position": "Junior Position",
                        "salary": "6 000 - 9 000 PLN",
                        "offerUrl": "https://exaple.com/offer/123"
                        }
                        """).contentType(MediaType.APPLICATION_JSON_VALUE));

        //then
        String createdOfferJson = performOnePostOffer.andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OfferResponseDto offerResponseDto = objectMapper.readValue(createdOfferJson, OfferResponseDto.class);
        String id = offerResponseDto.id();
        assertAll(
                () -> assertThat(offerResponseDto.companyName()).isEqualTo("ExampleCompany"),
                () -> assertThat(offerResponseDto.position()).isEqualTo("Junior Position"),
                () -> assertThat(offerResponseDto.salary()).isEqualTo("6 000 - 9 000 PLN"),
                () -> assertThat(offerResponseDto.offerUrl()).isEqualTo("https://exaple.com/offer/123"),
                () -> assertThat(id).isNotNull()
        );


        //step 17: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 5 offers
        // given & when
        ResultActions peformGetOffers = mockMvc.perform(get("/offers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        String oneOfferJson = peformGetOffers.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<OfferResponseDto> parsedJsonWithOneOffer = objectMapper.readValue(oneOfferJson, new TypeReference<>() {
        });
        assertThat(parsedJsonWithOneOffer).hasSize(5);
        assertThat(parsedJsonWithOneOffer.stream().map(OfferResponseDto::id)).contains(id);
    }
}
