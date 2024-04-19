package com.junioroffers.domain.offer;

import com.junioroffers.domain.offer.dto.JobOfferResponse;
import com.junioroffers.domain.offer.dto.OfferDto;
import com.junioroffers.domain.offer.dto.OfferResponseDto;


public class OfferMapper {


    public static Offer mapFromOfferDtoToOffer(OfferDto offerDto) {
        return Offer.builder()
                .companyName(offerDto.companyName())
                .position(offerDto.position())
                .salary(offerDto.salary())
                .offerUrl(offerDto.offerUrl())
                .build();
    }

    public static OfferResponseDto mapFromOfferToOfferDto(Offer offer) {
        return OfferResponseDto.builder()
                .id(offer.id())
                .companyName(offer.companyName())
                .position(offer.position())
                .salary(offer.salary())
                .offerUrl(offer.offerUrl())
                .build();
    }

    public static Offer mapFromJobOfferResponseToOffer(JobOfferResponse jobOfferResponse) {
        return Offer.builder()
                .companyName(jobOfferResponse.company())
                .position(jobOfferResponse.title())
                .salary(jobOfferResponse.salary())
                .offerUrl(jobOfferResponse.offerUrl())
                .build();
    }
}
