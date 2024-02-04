package com.junioroffers.domain.offer;

import java.util.List;
import java.util.Optional;

public interface OfferRepository {

    boolean existsByOfferUrl(String url);

    Offer save(Offer offer);

    Optional<Offer> findById(String id);

    List<Offer> findAll();

    List<Offer> saveAll(List<Offer> offers);
}