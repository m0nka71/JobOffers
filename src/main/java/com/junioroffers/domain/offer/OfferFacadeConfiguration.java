package com.junioroffers.domain.offer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
public class OfferFacadeConfiguration {

    @Bean
    OfferFacade offerFacade(OfferFetchable offerFetchable) {
        OfferRepository repository = new OfferRepository() {
            @Override
            public boolean existsByOfferUrl(String url) {
                return false;
            }

            @Override
            public Offer save(Offer offer) {
                return null;
            }

            @Override
            public Optional<Offer> findById(String id) {
                return Optional.empty();
            }

            @Override
            public List<Offer> findAll() {
                return null;
            }

            @Override
            public List<Offer> saveAll(List<Offer> offers) {
                return null;
            }
        };
        OfferService offerService = new OfferService(offerFetchable, repository);
        return new OfferFacade(repository, offerService);
    }
}
