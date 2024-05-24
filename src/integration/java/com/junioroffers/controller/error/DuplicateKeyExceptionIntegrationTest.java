package com.junioroffers.controller.error;

import com.junioroffers.BaseIntegrationTest;
import com.junioroffers.infrastructure.apivalidation.ApiValidationErrorDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class DuplicateKeyExceptionIntegrationTest extends BaseIntegrationTest {

    @Container
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

    @DynamicPropertySource
    public static void propertyOverride(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("offer.http.client.config.uri", () -> WIRE_MOCK_HOST);
        registry.add("offer.http.client.config.port", () -> wireMockServer.getPort());
    }

    @Test
    @WithMockUser
    public void should_return_409_conflict_when_offer_with_provided_url_already_exists() throws Exception {
        //step 1 - adding new offer
        //given && when
        ResultActions offer = mockMvc.perform(post("/offers")
                .content("""
                        {
                        "companyName": "ExampleCompany",
                        "position": "Junior Position",
                        "salary": "6 000 - 9 000 PLN",
                        "offerUrl": "https://exaple.com/offer/123"
                        }
                        """).contentType(MediaType.APPLICATION_JSON_VALUE + ";charset-UTF-8"));
        //then
        offer.andExpect(status().isCreated());

        //step 2 - adding duplicated offer
        //given && when
        ResultActions duplicatedOffer = mockMvc.perform(post("/offers")
                .content("""
                        {
                        "companyName": "ExampleCompany",
                        "position": "Junior Position",
                        "salary": "6 000 - 9 000 PLN",
                        "offerUrl": "https://exaple.com/offer/123"
                        }
                        """).contentType(MediaType.APPLICATION_JSON_VALUE + ";charset-UTF-8"));

        //then
        MvcResult mvcResult = duplicatedOffer.andExpect(status().isConflict()).andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        ApiValidationErrorDto result = objectMapper.readValue(json, ApiValidationErrorDto.class);
        assertThat(result.messages()).containsExactlyInAnyOrder(
                "Offer url already exists");
    }
}
