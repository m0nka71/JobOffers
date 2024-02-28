package com.junioroffers.domain.offer;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
class OfferService {

    private final OfferFetchable offerFetcher;
    private final OfferRepository offerRepository;


    List<Offer> fetchAllOffersAndSaveAllIfNotExists() {
        List<Offer> jobOffers = fetchOffers();
//        final List<Offer> offers = filterNotExistingOffers(jobOffers);
        try {
            return jobOffers;
//            return offerRepository.saveAll(offers);
        } catch (OfferAlreadyExistsException offerAlreadyExistsException) {
            throw new OfferSavingException(offerAlreadyExistsException.getMessage(), jobOffers);
        }
    }

    private List<Offer> filterNotExistingOffers(List<Offer> jobOffers) {
     return jobOffers.stream()
             .filter(offerDto -> !offerDto.url().isEmpty())
             .filter(offerDto -> !offerRepository.existsByOfferUrl(offerDto.url()))
             .toList();
    }

    private List<Offer> fetchOffers() {
        return offerFetcher.fetchOffers()
                .stream()
                .map(OfferMapper::mapFromJobOfferResponseToOffer)
                .toList();
    }
}
