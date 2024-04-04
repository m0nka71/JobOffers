package com.junioroffers.domain.offer;

import com.junioroffers.domain.offer.dto.JobOfferResponse;
import com.junioroffers.domain.offer.dto.OfferDto;
import com.junioroffers.domain.offer.dto.OfferResponseDto;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


public class OfferFacadeTest {

    OfferFacade offerFacade = new OfferFacadeTestConfiguration().offerFacadeForTests();

    @Test
    public void should_fetch_from_jobs_from_remote_and_save_all_offers_when_repository_is_empty() {
        // given
        assertThat(offerFacade.findAllOffers()).isEmpty();

        // when
        List<OfferResponseDto> result = offerFacade.fetchAllOffersAndSaveAllIfNotExists();

        // then
        assertThat(result).hasSize(6);
    }

    @Test
    public void should_save_only_2_offers_when_repository_had_4_added_with_offer_urls() {
        //given
        offerFacade = new OfferFacadeTestConfiguration(
                List.of(
                        new JobOfferResponse("Senior", "xyz", "123", "1"),
                        new JobOfferResponse("Junior", "xyz", "123", "2"),
                        new JobOfferResponse("Director", "xyz", "123", "3"),
                        new JobOfferResponse("Associate", "xyz", "123", "4"),
                        new JobOfferResponse("Senior", "Google", "4000", "https://someurl.pl/5"),
                        new JobOfferResponse("Manager", "Nokia", "7000", "https://someother.pl/6")
                )
        ).offerFacadeForTests();
        offerFacade.saveOffer(new OfferDto("Abc", "xxx", "123", "1"));
        offerFacade.saveOffer(new OfferDto("Abc", "xxx", "123", "2"));
        offerFacade.saveOffer(new OfferDto("Abc", "xxx", "123", "3"));
        offerFacade.saveOffer(new OfferDto("Abc", "xxx", "123", "4"));
        assertThat(offerFacade.findAllOffers()).hasSize(4);

        //when
        List<OfferResponseDto> offerResponseDtos = offerFacade.fetchAllOffersAndSaveAllIfNotExists();

        //then
        assertThat(List.of(
                offerResponseDtos.get(0).url(),
                offerResponseDtos.get(1).url()
        )).containsExactlyInAnyOrder("https://someurl.pl/5", "https://someother.pl/6");
    }


    @Test
    public void should_save_4_offers_when_there_are_no_offers_in_database() {
        //when
        offerFacade.saveOffer(new OfferDto("Abc", "X", "123", "example1.com"));
        offerFacade.saveOffer(new OfferDto("Abc", "X", "123", "example2.com"));
        offerFacade.saveOffer(new OfferDto("Abc", "X", "123", "example3.com"));
        offerFacade.saveOffer(new OfferDto("Abc", "X", "123", "example4.com"));

        //then
        assertThat(offerFacade.findAllOffers()).hasSize(4);
    }

    @Test
    public void should_throw_duplicate_key_exception_when_with_offer_url_exists() {
        //given
        OfferResponseDto savedOffer = offerFacade.saveOffer(new OfferDto("Abc", "X", "123", "example123.com"));
        String savedId = savedOffer.id();
        assertThat(offerFacade.findOfferById(savedId).id()).isEqualTo(savedId);

        //when
        Throwable thrown = catchThrowable(() -> offerFacade.saveOffer(
                new OfferDto("Xyz", "abc", "999", "example123.com")));

        //then
        AssertionsForClassTypes.assertThat(thrown)
                .isInstanceOf(OfferAlreadyExistsException.class)
                .hasMessage("Offer with offerUrl [example123.com] already exists");
    }

    @Test
    public void should_throw_not_found_exception_when_offer_not_found() {
        //given
        assertThat(offerFacade.findAllOffers()).isEmpty();

        //when
        Throwable thrown = catchThrowable(() -> offerFacade.findOfferById("000"));

        //then
        AssertionsForClassTypes.assertThat(thrown)
                .isInstanceOf(OfferNotFoundException.class)
                .hasMessage("Offer with id 000 not found");

    }

    @Test
    public void should_find_offer_by_id_when_offer_was_saved() {
        //given
        OfferResponseDto savedOffer = offerFacade.saveOffer(new OfferDto("Abc", "Xxx", "123", "example1.com"));

        //when
        OfferResponseDto offerById = offerFacade.findOfferById(savedOffer.id());

        //then
        assertThat(offerById).isEqualTo(OfferResponseDto.builder()
                .id(savedOffer.id())
                .companyName("Abc")
                .position("Xxx")
                .salary("123")
                .url("example1.com")
                .build()
                );
    }

}